#!/usr/bin/env bash
# ======================================================
# luminegram - iOS input audit
# usage: bash scripts/ios-audit.sh [<old-upstream-ref> <new-upstream-ref>]
# 1. counts the isIOSInputStyle() calls in ChatActivityEnterView and
#    compares them with scripts/ios-gates.count (exit 1 on a mismatch:
#    a merge dropped or duplicated a gate).
# 2. with two refs: lists the methods that upstream changed in that file
#    and that also hold a gate, so they are read before building.
# Update scripts/ios-gates.count in the commit that adds or removes a gate.
# ======================================================
set -u
cd "$(git rev-parse --show-toplevel)"
F=TMessagesProj/src/main/java/org/telegram/ui/Components/ChatActivityEnterView.java
EXPECT=$(cat scripts/ios-gates.count)
python3 - "$F" "$EXPECT" "${1:-}" "${2:-}" <<'PY'
import re, subprocess, sys
path, expect, old, new = sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4]
lines = open(path).read().split('\n')
sig = re.compile(r'^\s{4}(?:public|private|protected|static|final|synchronized|\s)*[\w<>\[\],.? ]+\s+(\w+)\s*\([^;]*\)\s*(?:throws [\w., ]+)?\{\s*$')
gates = []
for i, l in enumerate(lines):
    if 'isIOSInputStyle()' in l and 'public boolean isIOSInputStyle()' not in l:
        name = '?'
        for j in range(i, -1, -1):
            m = sig.match(lines[j])
            if m and not re.match(r'^\s*(if|for|while|switch|else|catch|synchronized)\b', lines[j]):
                name = m.group(1)
                break
        gates.append((i + 1, name))
print('gates found: %d, expected: %d' % (len(gates), expect))
status = 0
if len(gates) != expect:
    print('MISMATCH: a gate was dropped or duplicated')
    status = 1
if old and new:
    out = subprocess.run(['git', 'diff', '-U0', old, new, '--', path], capture_output=True, text=True).stdout
    up = subprocess.run(['git', 'show', new + ':' + path], capture_output=True, text=True).stdout.split('\n')
    def enclosing(src, i):
        for j in range(i, -1, -1):
            m = sig.match(src[j])
            if m and not re.match(r'^\s*(if|for|while|switch|else|catch|synchronized)\b', src[j]):
                return m.group(1)
        return '?'
    touched = set()
    for l in out.split('\n'):
        m = re.match(r'^@@ -\S+ \+(\d+)(?:,(\d+))? @@', l)
        if m:
            start = int(m.group(1))
            count = int(m.group(2)) if m.group(2) is not None else 1
            for n in range(start, start + max(count, 1)):
                if 0 < n <= len(up):
                    touched.add(enclosing(up, n - 1))
    hit = sorted({n for _, n in gates if n in touched})
    print('upstream changed %d methods in the enter view' % len(touched))
    print('methods with a gate that upstream changed: ' + (', '.join(hit) if hit else 'none'))
sys.exit(status)
PY
