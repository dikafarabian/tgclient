#!/usr/bin/env bash
# ======================================================
# luminegram - sync audit
# usage: bash scripts/sync-audit.sh <old-upstream-ref> <new-upstream-ref>
# Run after merging an upstream release, next to layer-check.sh.
# layer-check proves our hooks still exist; this shows what
# upstream changed around them and what can break without a
# merge conflict (renamed engines, removed imports).
# Exit 1 only for a hard compile risk (removed import).
# ======================================================
set -u
cd "$(git rev-parse --show-toplevel)"
OLD="${1:-}"
NEW="${2:-}"
if [ -z "$OLD" ] || [ -z "$NEW" ]; then
  echo "usage: bash scripts/sync-audit.sh <old-upstream-ref> <new-upstream-ref>"
  exit 2
fi
for r in "$OLD" "$NEW"; do
  if ! git cat-file -e "$r^{commit}" 2>/dev/null; then
    echo "missing ref $r: git fetch --shallow-since=<date> upstream master"
    exit 2
  fi
done

J=TMessagesProj/src/main/java
R=TMessagesProj/src/main/res
APP=TMessagesProj_App
HOOKS=$(grep -E '^"[^"|]+\|' scripts/layer-check.sh | sed -E 's/^"([^"|]+)\|.*/\1/' \
  | sed -e "s#\$J#$J#" -e "s#\$R#$R#" -e "s#\$APP#$APP#" \
  | grep -v '/app/lumine/' | sort -u)

echo "== 1. hook files changed upstream ($OLD..$NEW)"
FOUND=0
for f in $HOOKS; do
  stat=$(git diff --numstat "$OLD" "$NEW" -- "$f" 2>/dev/null | awk '{print "+"$1" -"$2}')
  if [ -n "$stat" ]; then
    echo "  $f  $stat"
    FOUND=1
  fi
done
[ $FOUND -eq 0 ] && echo "  none"

echo "== 2. section engine tokens in added upstream lines"
TOKENS=(setLegacySections setSectionsDrawBackground setCaptureSectionsDecoratorAllowed ItemDecoration BitmapSections TAG_NOT_SECTION "setSections(" "extends RecyclerListView" UniversalAdapter)
DIFF=$(git diff -U0 "$OLD" "$NEW" -- '*.java')
for t in "${TOKENS[@]}"; do
  n=$(printf '%s\n' "$DIFF" | grep -E '^\+[^+]' | grep -cF "$t")
  [ "$n" -gt 0 ] && echo "  $t: $n added lines"
done
echo "  files using setSectionsDrawBackground(true): $(git grep -lF 'setSectionsDrawBackground(true)' "$OLD" -- '*.java' | wc -l) -> $(git grep -lF 'setSectionsDrawBackground(true)' "$NEW" -- '*.java' | wc -l)"
echo "  files using setLegacySections(false): $(git grep -lF 'setLegacySections(false)' "$OLD" -- '*.java' | wc -l) -> $(git grep -lF 'setLegacySections(false)' "$NEW" -- '*.java' | wc -l)"
echo "  new files named like a sections engine:"
NEWENG=$(git diff --name-status --diff-filter=A "$OLD" "$NEW" | grep -iE 'section|decoration|segment' | sed 's/^/    /')
[ -n "$NEWENG" ] && printf '%s\n' "$NEWENG" || echo "    none"

echo "== 3. imports removed upstream but still used by our code"
python3 - "$OLD" "$NEW" $HOOKS <<'EOF'
import re, subprocess, sys
old, new, files = sys.argv[1], sys.argv[2], [f for f in sys.argv[3:] if f.endswith('.java')]
bad = 0
for f in files:
    d = subprocess.run(['git', 'diff', '-U0', old, new, '--', f], capture_output=True, text=True).stdout
    removed = set(re.findall(r'^-import ([\w.]+);', d, re.M))
    added = set(re.findall(r'^\+import ([\w.]+);', d, re.M))
    try:
        src = open(f, encoding='utf-8').read()
    except OSError:
        continue
    body = re.sub(r'^import .*$', '', src, flags=re.M)
    imported = set(re.findall(r'^import ([\w.]+);', src, re.M))
    for imp in sorted(removed - added):
        if imp in imported:
            continue
        name = imp.rsplit('.', 1)[-1]
        pkg = imp.rsplit('.', 1)[0]
        same_pkg = re.search(r'^package ' + re.escape(pkg) + ';', src, re.M)
        if not same_pkg and re.search(r'\b' + re.escape(name) + r'\b', body):
            print('  %s: %s removed upstream, still used' % (f, imp))
            bad += 1
if not bad:
    print('  none')
sys.exit(1 if bad else 0)
EOF
RC=$?
echo "== done (exit $RC)"
exit $RC
