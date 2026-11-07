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
"$R/values/lumine_strings.xml"
"$R/drawable/lumine_settings_sparkle.xml"
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
"$J/org/telegram/ui/SettingsActivity.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/ui/SettingsActivity.java|import app.lumine.settings.LumineSettingsActivity;"
"$J/org/telegram/ui/SettingsActivity.java|presentSettingFragment(new LumineSettingsActivity());"
"$J/org/telegram/ui/SettingsActivity.java|R.drawable.lumine_settings_sparkle"
"$J/org/telegram/ui/SettingsActivity.java|LumineStrings.get(R.string.LumineSettings)"
"$J/org/telegram/messenger/AndroidUtilities.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/messenger/ApplicationLoader.java|BuildVars.BUILD_COMMIT"
"$J/org/telegram/ui/IntroActivity.java|paint.setColor(0xFFFFFFFF);"
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
