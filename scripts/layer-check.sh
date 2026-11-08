#!/usr/bin/env bash
# ======================================================
# luminegram - layer check
# usage: bash scripts/layer-check.sh
# Verifies that our own files exist, that every hook we
# placed in upstream files is still there, and that no
# official Telegram identity or secret came back.
# Run after every upstream merge. Exit 0 = layer OK.
# ======================================================
set -u
cd "$(git rev-parse --show-toplevel)"
J=TMessagesProj/src/main/java
R=TMessagesProj/src/main/res
APP=TMessagesProj_App

OWN_FILES=(
"$J/app/lumine/LumineConfig.java"
"$J/app/lumine/LumineStrings.java"
"$J/app/lumine/settings/LumineSettingsActivity.java"
"$J/app/lumine/settings/LumineTabBar.java"
"$J/app/lumine/settings/LumineHomePage.java"
"$J/app/lumine/settings/LumineComingSoonPage.java"
"$J/app/lumine/push/LuminePushLog.java"
"$J/app/lumine/push/PushConfig.java"
"$J/app/lumine/push/PushStatus.java"
"$J/app/lumine/push/PushSwitcher.java"
"$J/app/lumine/settings/LumineCards.java"
"$J/app/lumine/settings/LumineGeneralPage.java"
"$J/app/lumine/settings/LumineAppearancePage.java"
"$J/app/lumine/settings/LumineNotificationActivity.java"
"$J/app/lumine/md3/LumineMd3.java"
"$J/app/lumine/md3/LumineTonalCard.java"
"$J/app/lumine/md3/LumineSwitchDraw.java"
"$J/app/lumine/md3/LumineSliderDraw.java"
"$J/app/lumine/md3/LumineListItems.java"
"$J/app/lumine/tabs/LumineTabs.java"
"$J/app/lumine/settings/LumineTabsActivity.java"
"$J/app/lumine/settings/LumineIconRow.java"
"$J/app/lumine/stealth/LumineStealth.java"
"$J/app/lumine/stealth/LumineIdDc.java"
"$J/app/lumine/ghost/LumineGhost.java"
"$J/app/lumine/ghost/LumineGhostRequests.java"
"$J/app/lumine/ghost/LumineGhostStory.java"
"$J/app/lumine/crash/LumineCrashLog.java"
"$J/app/lumine/crash/LumineCrashActivity.java"
"$J/app/lumine/settings/LumineGhostActivity.java"
"$J/app/lumine/settings/LumineStealthPage.java"
"$APP/src/main/res/raw/lumine_keep.xml"
"$R/values/lumine_strings.xml"
"$R/drawable/lumine_settings_sparkle.xml"
"$R/drawable/lumine_ic_notification.xml"
"$R/drawable/lumine_ic_account.xml"
"$R/drawable/lumine_tab_home.xml"
"$R/drawable/lumine_tab_general.xml"
"$R/drawable/lumine_tab_appearance.xml"
"$R/drawable/lumine_tab_stealth.xml"
"$R/drawable/lumine_tab_experimental.xml"
"$R/drawable/lumine_icon_foreground.xml"
".github/workflows/apk.yml"
"scripts/ci/version.ts"
"scripts/ci/post-card.ts"
"scripts/ci/upload.sh"
"scripts/layer-check.sh"
)

MARKERS=(
"gradle.properties|APP_PACKAGE=com.lumine.gram"
"TMessagesProj/build.gradle|getSecret(\"TELEGRAM_APP_ID\")"
"TMessagesProj/build.gradle|getSecret(\"TELEGRAM_APP_HASH\")"
"TMessagesProj/build.gradle|\"BUILD_COMMIT\""
"TMessagesProj/build.gradle|\"BUILD_TYPE_NAME\""
"TMessagesProj/build.gradle|ABI_FILTERS"
"$APP/build.gradle|canary {"
"$APP/build.gradle|sourceSets.canary"
"$APP/build.gradle|abiFilterList"
"$J/org/telegram/messenger/BuildVars.java|BuildConfig.APP_ID"
"$J/org/telegram/messenger/BuildVars.java|BuildConfig.APP_HASH"
"$J/org/telegram/messenger/BuildVars.java|BuildConfig.BUILD_COMMIT"
"$J/org/telegram/messenger/BuildVars.java|BuildConfig.BUILD_TYPE_NAME"
"$J/org/telegram/messenger/PushListenerController.java|LuminePushLog.tokenFailed"
"$J/org/telegram/messenger/PushListenerController.java|LuminePushLog.pushReceived"
"$J/org/telegram/messenger/MessagesController.java|LuminePushLog.registerResult"
"$J/org/telegram/messenger/ApplicationLoader.java|app.lumine.push.PushConfig.createProvider"
"$J/org/telegram/messenger/GcmPushListenerService.java|app.lumine.push.PushConfig.usesGoogle"
"$J/org/telegram/ui/SettingsActivity.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/ui/SettingsActivity.java|import app.lumine.settings.LumineSettingsActivity;"
"$J/org/telegram/ui/SettingsActivity.java|presentSettingFragment(new LumineSettingsActivity());"
"$J/org/telegram/ui/SettingsActivity.java|R.drawable.lumine_settings_sparkle"
"$J/org/telegram/ui/SettingsActivity.java|LumineStrings.get(R.string.LumineSettings)"
"$J/org/telegram/messenger/AndroidUtilities.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/tgnet/ConnectionsManager.java|LumineGhostRequests.blocks"
"$J/org/telegram/tgnet/ConnectionsManager.java|LumineGhostRequests.wrap"
"$J/org/telegram/ui/MainTabsActivity.java|layoutSearchGroup"
"$J/org/telegram/messenger/ApplicationLoader.java|LumineCrashLog.install"
"$J/org/telegram/messenger/ApplicationLoader.java|LumineCrashLog.isCrashProcess"
"TMessagesProj/src/main/AndroidManifest.xml|app.lumine.crash.LumineCrashActivity"
"$J/org/telegram/ui/Stories/StoryViewer.java|LumineGhostStory.shouldAsk"
"$J/org/telegram/ui/Stories/StoryViewer.java|releaseStoryGhostSession"
"$J/org/telegram/messenger/ApplicationLoader.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/ui/IntroActivity.java|paint.setColor(0xFFFFFFFF);"
"$J/org/telegram/ui/Components/Switch.java|app.lumine.md3.LumineSwitchDraw.draw"
"$J/org/telegram/ui/Components/Switch.java|app.lumine.md3.LumineSwitchDraw.measuredSize"
"$J/org/telegram/ui/Components/SeekBarView.java|app.lumine.md3.LumineSliderDraw.draw"
"$J/org/telegram/ui/Cells/TextCell.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Cells/TextCheckCell.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Cells/TextDetailSettingsCell.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Cells/TextSettingsCell.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Components/UniversalAdapter.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Components/UniversalAdapter.java|viewType == VIEW_TYPE_ANIMATED_HEADER) && app.lumine.md3.LumineListItems.enabled()"
"$J/org/telegram/ui/Components/RecyclerListView.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Components/UniversalRecyclerView.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/Components/RecyclerListView.java|isSegmentedHeader"
"$J/org/telegram/ui/Components/RecyclerListView.java|view instanceof HeaderCell ? 1 : 0"
"$J/org/telegram/ui/SettingsActivity.java|app.lumine.md3.LumineListItems.tonalBackground"
"$J/org/telegram/messenger/LocaleController.java|app.lumine.stealth.LumineStealth.relativeLastSeen"
"$J/org/telegram/ui/ProfileActivity.java|shortStatus ? new boolean[1] : null, true)"
"$J/org/telegram/ui/ProfileActivity.java|app.lumine.stealth.LumineIdDc.enabled"
"$J/org/telegram/ui/ProfileActivity.java|app.lumine.stealth.LumineIdDc.showSheet"
"$J/org/telegram/ui/ProfileActivity.java|app.lumine.stealth.LumineIdDc.idText"
"$J/org/telegram/ui/ProfileActivity.java|put(++pointer, idDcRow, sparseIntArray);"
"$J/org/telegram/ui/Cells/ProfileSearchCell.java|app.lumine.stealth.LumineStealth.hidePhone"
"$J/org/telegram/ui/SettingsActivity.java|app.lumine.stealth.LumineStealth.hidePhone"
"$J/org/telegram/ui/Components/ChatAvatarContainer.java|statusMadeShorter : null, true)"
"$J/org/telegram/messenger/AndroidUtilities.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/ActionBar/Theme.java|app.lumine.md3.LumineListItems"
"$J/org/telegram/ui/MainTabsActivity.java|import app.lumine.tabs.LumineTabs;"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.count()"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.typeAt(position)"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.position(LumineTabs.Type.values()[index])"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.applyCallsTabToggle"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.addListener(lumineTabsListener)"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.removeListener(lumineTabsListener)"
"$J/org/telegram/ui/MainTabsActivity.java|LumineTabs.titlesVisible()"
"$J/org/telegram/ui/Components/glass/GlassTabView.java|public void setTitleVisible"
"$J/org/telegram/ui/MainTabsActivity.java|onSearchButtonClicked()"
"$J/org/telegram/ui/MainTabsActivity.java|animatorTabsScrollHide"
"$J/org/telegram/ui/MainTabsActivity.java|!isBottomBarHidden()"
"$J/org/telegram/ui/DialogsActivity.java|mainTabsActivityController.setTabsScrollHide(hideTabsOnScroll)"
"$J/org/telegram/ui/DialogsActivity.java|app.lumine.tabs.LumineTabs.searchButton()"
"$J/org/telegram/ui/DialogsActivity.java|app.lumine.tabs.LumineTabs.isEnabled"
"$R/values/strings.xml|<string name=\"AppName\">luminegram</string>"
"$R/values/strings.xml|<string name=\"AppNameBeta\">luminegram</string>"
"$R/mipmap-anydpi-v26/ic_launcher.xml|lumine_icon_foreground"
"$R/mipmap-anydpi-v26/ic_launcher_round.xml|lumine_icon_foreground"
)

ABSENT=(
"TMessagesProj/build.gradle|com.google.gms.google-services"
"$J/org/telegram/messenger/BuildVars.java|014b35b6184100b085b0d0572f9b5103"
"$J/org/telegram/messenger/BuildVars.java|APP_ID = 4;"
"TMessagesProj_App/src/main/java/org/telegram/messenger/ApplicationLoaderImpl.java|isStandalone"
"TMessagesProj_App/src/main/java/org/telegram/messenger/ApplicationLoaderImpl.java|isBeta"
)

FORBIDDEN_TRACKED='google-services\.json$|\.keystore$|\.jks$'

fail=0

for f in "${OWN_FILES[@]}"; do
  if [ ! -f "$f" ]; then echo "MISSING FILE [$f]"; fail=1; fi
done

for m in "${MARKERS[@]}"; do
  f="${m%%|*}"; k="${m#*|}"
  if [ ! -f "$f" ]; then echo "MISSING FILE [$f] (marker: $k)"; fail=1
  elif ! grep -qF -- "$k" "$f"; then echo "MISSING MARKER [$f] $k"; fail=1; fi
done

for m in "${ABSENT[@]}"; do
  f="${m%%|*}"; k="${m#*|}"
  if [ -f "$f" ] && grep -qF -- "$k" "$f"; then echo "UNEXPECTED [$f] $k"; fail=1; fi
done

tracked="$(git ls-files | grep -E "$FORBIDDEN_TRACKED" || true)"
if [ -n "$tracked" ]; then echo "TRACKED SECRET FILES:"; echo "$tracked"; fail=1; fi

eff="$(grep -E '^APP_PACKAGE=' gradle.properties | tail -1 | cut -d= -f2)"
if [ "$eff" != "com.lumine.gram" ]; then echo "EFFECTIVE APP_PACKAGE is [$eff]"; fail=1; fi

if [ "$fail" -eq 0 ]; then echo "layer OK (${#MARKERS[@]} markers, ${#OWN_FILES[@]} files)"; fi
exit "$fail"
