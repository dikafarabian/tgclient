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

OWN_FILES="$K/app/lumine/ios/IosStyleConfig.kt $K/app/lumine/ios/IosStyleHelper.kt $K/app/lumine/ios/UnreadBackBadgeHelper.kt $J/app/lumine/ios/ChatHeaderMetrics.java $K/app/lumine/ios/IosStyleSettingsActivity.kt $K/app/lumine/ios/IosChatPreview.kt $K/app/lumine/ios/IosPreviewRowsAdapter.kt $K/app/lumine/ios/IosCheckerDrawable.kt $J/app/lumine/icons/IconPackManager.java $K/app/lumine/icons/IconPacksConfig.kt $K/app/lumine/md3/Md3Config.kt $K/app/lumine/md3/Md3ListItems.kt $J/app/lumine/md3/ConfigCellMd3Group.java $J/app/lumine/md3/ConfigCellMd3Option.java $J/app/lumine/chats/InputTextAnimation.java $J/app/lumine/chats/ActionButtonStyle.java"

PATCHED_FILES="$J/org/telegram/ui/ActionBar/ActionBar.java $J/org/telegram/ui/ActionBar/BackDrawable.java $J/org/telegram/ui/ActionBar/SimpleTextView.java $J/org/telegram/ui/Components/ChatAvatarContainer.java $J/org/telegram/ui/Components/CounterView.java $J/org/telegram/ui/ChatActivity.java $J/org/telegram/ui/ComposeDrawable.java $J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java TMessagesProj/src/main/res/values/strings_lumine.xml $J/org/telegram/messenger/ApplicationLoader.java $J/tw/nekomimi/nekogram/NekoConfig.java $J/org/telegram/ui/Components/ChatActivityEnterView.java $J/org/telegram/ui/Components/EditTextBoldCursor.java"

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
"$J/org/telegram/ui/Components/ChatActivityEnterView.java|app.lumine.ios.IosStyleHelper.sendAsLongPressEnabled"
"$J/org/telegram/ui/Components/ChatActivityEnterView.java|private void setSendAsShown"
"$J/org/telegram/ui/Components/ChatActivityEnterView.java|private boolean isSendAsSlotChat"
"$J/org/telegram/ui/Components/EditTextBoldCursor.java|private static CharSequence ellipsizeHint"
"$J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java|IosStyleSettingsActivity"
"TMessagesProj/src/main/res/values/strings_lumine.xml|LumineIosStyle"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.ios.IosStyleConfig.init"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.icons.IconPacksConfig.init"
"$J/tw/nekomimi/nekogram/ui/icons/IconsResources.java|IconPackManager.getInstance().getDrawable"
"TMessagesProj/src/main/AndroidManifest.xml|app.lumine.icons.IconPackProvider"
"TMessagesProj/build.gradle|androidsvg-aar"
"$J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java|app.lumine.icons.IconPacksActivity"
"$J/tw/nekomimi/nekogram/helpers/SettingsBackupHelper.java|LumineIconPacks"
"$J/tw/nekomimi/nekogram/helpers/SettingsHelper.java|app.lumine.icons.IconPacksActivity"
"$J/org/telegram/ui/Components/ChatActivityEnterViewAnimatedIconView.java|staticIcons"
"$J/tw/nekomimi/nekogram/NekoConfig.java|\"unreadBadgeOnBackButton\", configTypeBool, true"
"$J/org/telegram/ui/Cells/TextCell.java|app.lumine.md3.Md3ListItems.rowHeight"
"$J/org/telegram/ui/Cells/TextCheckCell.java|app.lumine.md3.Md3ListItems.detailRowHeight"
"$J/org/telegram/ui/Cells/TextSettingsCell.java|app.lumine.md3.Md3ListItems.rowHeight"
"$J/org/telegram/ui/Cells/TextDetailSettingsCell.java|app.lumine.md3.Md3ListItems.detailRowHeight"
"$J/org/telegram/ui/Components/UniversalAdapter.java|app.lumine.md3.Md3ListItems.shadowHeight"
"$J/org/telegram/ui/Components/RecyclerListView.java|private boolean useSegmentedSections"
"$J/org/telegram/ui/Components/RecyclerListView.java|drawSegmentedSectionsBackgrounds(canvas)"
"$J/org/telegram/ui/ActionBar/Theme.java|app.lumine.md3.Md3ListItems.enabled()"
"$J/org/telegram/messenger/AndroidUtilities.java|app.lumine.md3.Md3ListItems.enabled()"
"$J/org/telegram/ui/SettingsActivity.java|app.lumine.md3.Md3ListItems.tonalBackground"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.md3.Md3Config.init"
"$J/tw/nekomimi/nekogram/settings/NekoAppearanceSettingsActivity.java|ConfigCellMd3Group"
"TMessagesProj/src/main/res/values/strings_lumine.xml|LumineMaterialDesign3"
"$J/com/radolyn/ayugram/AyuSaveExclusions.java|repairStoredIds"
"$J/com/radolyn/ayugram/utils/AyuGhostUtils.java|new TLRPC.TL_messages_messageViews()"
"$J/tw/nekomimi/nekogram/helpers/SettingsBackupHelper.java|parseLegacySet"
"$J/org/telegram/ui/Components/AnimatedFileDrawable.java|AndroidUtilities.runOnUIThread(this::invalidateInternal)"
"$J/org/telegram/ui/Components/EditTextEffects.java|public final void setTextWithoutInputAnimation"
"$J/org/telegram/ui/Components/EditTextEffects.java|inputTextAnimation.beforeDraw"
"$J/org/telegram/ui/Components/EditTextBoldCursor.java|private void drawWithCursor"
"$J/org/telegram/ui/Components/ChatActivityEnterView.java|messageEditText.setTextWithoutInputAnimation"
"$J/org/telegram/ui/ChatActivity.java|private boolean isHeaderAttachSuppressed"
"$J/org/telegram/ui/Components/ChatActivityEnterView.java|app.lumine.chats.ActionButtonStyle.resolveIconColor"
"$K/app/lumine/ios/IosStyleSettingsActivity.kt|BUTTON_ACTION_STYLE"
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
