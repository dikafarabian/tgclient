#!/usr/bin/env bash
# ======================================================
# luminegram - iOS layer: marker check and patch export
# usage: bash scripts/ios-layer.sh check
#        bash scripts/ios-layer.sh export [base-ref]   (default: upstream/canary)
# ======================================================
set -u
cd "$(git rev-parse --show-toplevel)"
J=TMessagesProj/src/main/java
K=TMessagesProj/src/main/kotlin

OWN_FILES="$K/app/lumine/ios/IosStyleConfig.kt $K/app/lumine/ios/IosStyleHelper.kt $K/app/lumine/ios/UnreadBackBadgeHelper.kt $J/app/lumine/ios/ChatHeaderMetrics.java $K/app/lumine/ios/IosStyleSettingsActivity.kt $K/app/lumine/ios/IosChatPreview.kt $K/app/lumine/ios/IosPreviewRowsAdapter.kt $K/app/lumine/ios/IosCheckerDrawable.kt $J/app/lumine/icons/IconPackManager.java $K/app/lumine/icons/IconPacksConfig.kt"

PATCHED_FILES="$J/org/telegram/ui/ActionBar/ActionBar.java $J/org/telegram/ui/ActionBar/BackDrawable.java $J/org/telegram/ui/ActionBar/SimpleTextView.java $J/org/telegram/ui/Components/ChatAvatarContainer.java $J/org/telegram/ui/Components/CounterView.java $J/org/telegram/ui/ChatActivity.java $J/org/telegram/ui/ComposeDrawable.java $J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java TMessagesProj/src/main/res/values/strings_lumine.xml $J/org/telegram/messenger/ApplicationLoader.java $J/tw/nekomimi/nekogram/NekoConfig.java"

MARKERS=(
"$J/org/telegram/ui/ActionBar/ActionBar.java|app.lumine.ios.IosStyleHelper.compactChatPill"
"$J/org/telegram/ui/ActionBar/ActionBar.java|public void updateGlassBounds"
"$J/org/telegram/ui/ActionBar/ActionBar.java|public void updateBackPillWidth"
"$J/org/telegram/ui/ActionBar/ActionBar.java|app.lumine.ios.ChatHeaderMetrics"
"$J/org/telegram/ui/ActionBar/BackDrawable.java|public void setMoveBackIcon"
"$J/org/telegram/ui/ActionBar/SimpleTextView.java|public float getExactContentWidth"
"$J/org/telegram/ui/ActionBar/SimpleTextView.java|public int getCenteringDrawableBias"
"$J/org/telegram/ui/Components/ChatAvatarContainer.java|public boolean isIosMenuSlotAvatar"
"$J/org/telegram/ui/Components/ChatAvatarContainer.java|public float getCenteredTextWidth"
"$J/org/telegram/ui/Components/CounterView.java|public void setBackgroundSize"
"$J/org/telegram/ui/ChatActivity.java|public void updateMenuSlotAvatar"
"$J/org/telegram/ui/ChatActivity.java|app.lumine.ios.UnreadBackBadgeHelper.update"
"$J/org/telegram/ui/ChatActivity.java|avatarContainer.setActionBar(actionBar);"
"$J/org/telegram/ui/ComposeDrawable.java|public void setHideWhen"
"$J/org/telegram/ui/ChatActivity.java|public boolean stylePreview;"
"$J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java|IosStyleSettingsActivity"
"TMessagesProj/src/main/res/values/strings_lumine.xml|LumineIosStyle"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.ios.IosStyleConfig.init"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.icons.IconPacksConfig.init"
"$J/tw/nekomimi/nekogram/ui/icons/IconsResources.java|IconPackManager.getInstance().getDrawable"
"TMessagesProj/src/main/AndroidManifest.xml|app.lumine.icons.IconPackProvider"
"TMessagesProj/build.gradle|androidsvg-aar"
"$J/tw/nekomimi/nekogram/NekoConfig.java|\"unreadBadgeOnBackButton\", configTypeBool, true"
)

check() {
  local bad=0
  for f in $OWN_FILES; do
    [ -f "$f" ] || { echo "MISSING FILE $f"; bad=1; }
  done
  for m in "${MARKERS[@]}"; do
    f="${m%%|*}"; pat="${m#*|}"
    if ! grep -qF -- "$pat" "$f" 2>/dev/null; then
      echo "MISSING MARKER [$pat] in $f"; bad=1
    fi
  done
  if git grep -nIq "ChatHeaderUiHelper\|app\.luminegram\." -- $J $K; then
    echo "FOREIGN REF (exteraless leftovers):"
    git grep -nI "ChatHeaderUiHelper\|app\.luminegram\." -- $J $K | cut -c1-160
    bad=1
  fi
  [ $bad -eq 0 ] && echo "iOS layer OK"
  return $bad
}

export_patch() {
  local base="${1:-upstream/canary}"
  mkdir -p patches
  git diff "$base" HEAD -- $PATCHED_FILES > patches/ios-layer.patch
  echo "wrote patches/ios-layer.patch against $base ($(wc -l < patches/ios-layer.patch) lines)"
}

case "${1:-check}" in
  check) check ;;
  export) export_patch "${2:-}" ;;
  *) echo "usage: $0 check|export [base-ref]"; exit 2 ;;
esac
