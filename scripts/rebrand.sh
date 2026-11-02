#!/usr/bin/env bash
# ======================================================
# luminegram - rename NagramXF identifiers to luminegram
# ======================================================

set -euo pipefail

cd "$(git rev-parse --show-toplevel)"

export OLD_PKG=fork.risin42.nagramx
export NEW_PKG=com.lumine.gram

{ git grep -lIz -F "$OLD_PKG" -- . ':!scripts/rebrand.sh' ':!LICENSE' || true; } \
  | xargs -0 -r perl -pi -e 's/\Q$ENV{OLD_PKG}\E/$ENV{NEW_PKG}/g'

git ls-files -z 'TMessagesProj/src/*/res/values*/*.xml' \
  | xargs -0 -r perl -0pi -e '
      s{>([^<]*)<}{
        my $t = $1;
        $t =~ s/(?<![\@\w\/.])(?:Nagram ?XF|Nagram ?X|Nagram)(?!\w)/luminegram/g;
        ">$t<"
      }ge'

sub() {
  perl -pi -e "$2" "$1"
}

sub TMessagesProj/build.gradle "s/gramName = 'NagramXF'/gramName = 'luminegram'/"
sub TMessagesProj/src/main/java/tw/nekomimi/nekogram/settings/NekoAboutActivity.java 's/"Nagram XF v"/"luminegram v"/'
sub TMessagesProj/src/main/java/tw/nekomimi/nekogram/utils/AndroidUtil.java 's/"Nagram XF v"/"luminegram v"/'
sub TMessagesProj/src/main/java/com/exteragram/messenger/utils/text/LocaleUtils.java 's/return "Nagram XF";/return "luminegram";/'
sub TMessagesProj/src/main/kotlin/xyz/nextalone/nagram/NaConfig.kt 's/^(\s+)"Nagram XF"$/$1"luminegram"/'
sub TMessagesProj/src/main/java/org/telegram/ui/ProfileActivity.java 's/"NagramX-"/"luminegram-"/'
sub TMessagesProj/src/main/java/tw/nekomimi/nekogram/filters/RegexFiltersSettingActivity.java 's/"NagramXF Filters"/"luminegram Filters"/'
sub TMessagesProj/src/main/java/org/telegram/messenger/AndroidUtilities.java 's/DIRECTORY_PICTURES\), "Nagram"\)/DIRECTORY_PICTURES), "luminegram")/'
sub TMessagesProj/src/main/java/tw/nekomimi/nekogram/NekoConfig.java 's/"customSavePath", configTypeString, "Nagram"\)/"customSavePath", configTypeString, "luminegram")/'
sub TMessagesProj/src/main/AndroidManifest.xml 's/android:process=":nagramx"/android:process=":luminegram"/'
sub TMessagesProj/src/main/java/tw/nekomimi/nekogram/helpers/AppRestartHelper.java 's/"nagramx_(restart_intents|main_process_pid)"/"luminegram_$1"/'
sub TMessagesProj/src/debug/assets/plugins/smoke_test.plugin 's/__author__ = "Nagram XF"/__author__ = "luminegram"/'
sub Tools/generate-plugin-api-snapshot.ps1 "s/'NagramXF-plugin-api-snapshot/'luminegram-plugin-api-snapshot/"
