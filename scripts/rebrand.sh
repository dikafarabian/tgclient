#!/usr/bin/env bash
# ======================================================
# luminegram - rename exteraless identifiers to luminegram
# ======================================================

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

OLD=exteraless
NEW=luminegram

move_tracked() {
  local from=$1 to=$2
  mkdir -p "$(dirname "$to")"
  git mv -k "$from" "$to"
}

git ls-files | grep -i "$OLD" | grep -v '^\.gitmodules$' | while read -r path; do
  new_path=$(printf '%s' "$path" | sed "s/$OLD/$NEW/g; s/Exteraless/Luminegram/g; s/EXTERALESS/LUMINEGRAM/g")
  [ "$path" = "$new_path" ] || move_tracked "$path" "$new_path"
done

git ls-files -z | while IFS= read -r -d '' path; do
  case "$path" in
    .gitmodules|LICENSE|scripts/rebrand.sh|*.png|*.jpg|*.webp|*.so|*.jar|*.ttf|*.otf|*.mp3|*.ogg|*.tgs|*.zip) continue ;;
  esac
  [ -f "$path" ] || continue
  grep -qi "$OLD" "$path" 2>/dev/null || continue
  perl -0pi -e '
    s{exteraless/exteraless}{\x01REPO\x01}g;
    s{github\.com/exteraless}{\x01GH\x01}g;
    s{\@exteraless}{\x01AT\x01}g;
    s{openByUserName\("exteraless"}{\x01USER\x01}g;
    s{exteraless/build\.sh}{\x01BUILD\x01}g;
    s{exteralesscloudbot}{\x01CLOUD\x01}g;
    s{exteraless}{luminegram}g;
    s{Exteraless}{Luminegram}g;
    s{EXTERALESS}{LUMINEGRAM}g;
    s{\x01GH\x01}{github.com/exteraless}g;
    s{\x01REPO\x01}{exteraless/exteraless}g;
    s{\x01AT\x01}{\@exteraless}g;
    s{\x01USER\x01}{openByUserName("exteraless"}g;
    s{\x01BUILD\x01}{exteraless/build.sh}g;
    s{\x01CLOUD\x01}{exteralesscloudbot}g;
  ' "$path"
done
