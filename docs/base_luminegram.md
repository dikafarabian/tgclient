# base-luminegram

Branch `base-luminegram` of `dikafarabian/tgclient`. General rules (working style, git conventions, sandbox facts) are in `tgclient_project.md`; this document only covers what is specific to this base.

Last checked against the tree: 2026-11-07 (head `f8b231d0` before this edit). This is the fresh base: it starts from official Telegram for Android, not from another fork. `base-nagramxf` stays in the repo as a reference to read features from; features are ported by hand, never merged.

Verification state (layers as in section 8 of `tgclient_project.md`):
- Built by CI and confirmed by the owner on a device: a canary APK (arm64-v8a) builds, installs and runs, and the Telegram card arrives with the new format (owner screenshot, 2026-11-07, build `65b3ba2`). The commit hash in the card is monospace (owner confirmed).
- Built by CI, owner reported "berhasil" without per-feature detail: the later commits (version text with commit hash, app name, launcher icons, wordmark, intro). Treat these as unconfirmed on the device until the owner sends screenshots: launcher icon shape on the owner's launcher, themed (monochrome) icon, the `luminegram` wordmark on login / chat list / stories row, intro page animation with the sparkle (the old plane used to fly over the sphere), intro sphere on the light theme (white sphere on white background), the version line in Settings.
- Built by CI and confirmed on the owner device (screenshots 2026-11-08, builds `f8b231d` and `cfdfd3c`, canary arm64-v8a): the luminegram settings page with the floating tab bar, the General tab layout, the Home cards, the reworked Home push card on FCM (Service FCM, Status Ready in green, Last push time, no Error row) and the General "Push status" row after the fix (`FCM · today at 01:01`, before the fix `FCM · no token`). Confirmed in a later session (build `cfdfd3c`): the row in the main Settings, the push service dialog and the In-app Home card (Service In-app service, State Running). Not yet confirmed: the Notification page, the Home card opening it, the General row, and the Details card.
- Confirmed on the device (Home card and copied push report, 2026-11-08, build `f8b231d`): FCM works. Token 142 chars, `account 0 registered: true`, about 25 `push received` events between 23:44 and 00:20, last error none. The first event in that log is `token failed: Default FirebaseApp is not initialized` (23:34); a token arrives at 23:40. This matches the fix `4b7ca117` (keep Firebase config strings), but the log has no build id, so the order is consistent, not proven. Not verified at all: the push service switch (In-app and the unregister path of `PushSwitcher`), the `release` build type, the `armeabi-v7a` ABI, the sync procedure on a real upstream update (only simulated, section 4).

## 1. Identity

- Upstream: https://github.com/DrKLO/Telegram, branch `master` (official Telegram for Android). Remote name `upstream`. License GPL-3.0 (the repo is public; distributing an APK means the source must stay open).
- Sync point: `3de37c8cf` "update to 13.1.1 (7237)", 2026-10-25. Upstream had nothing newer on 2026-11-07 (`git ls-remote` returned the same sha).
- Upstream history is kept in full (about 650 commits; the official repo is small because releases land as one commit "update to X.Y.Z (code)" plus merged pull requests). The merge-base is real, so a plain `git merge` works.
- App: name `luminegram` (lowercase), package `com.lumine.gram`. API ID/hash come from CI secrets (never in the repo). Brand: sparkle logo, black sparkle on a white circle (colors differ from `base-nagramxf`, which used white on `#FA456C`).
- Own commits on top of upstream: 34 at head `f8b231d0` (20 at `77bb7854` plus 14 after it; `git rev-list --count upstream/master..HEAD`).
- Language: Java only. There are no Kotlin sources in `TMessagesProj` and the Kotlin plugin is not applied there (only `buildSrc` and the root classpath use Kotlin). Adding Kotlin to the module is a build change of its own; do it only when a feature needs it.

## 2. Structure of the upstream project (read from the tree, 2026-11-07)

- `TMessagesProj` is the Android library with all code, resources and the native code (`jni`, CMake target `tmessages.49`). Namespace `org.telegram.messenger`.
- `TMessagesProj_App` is the app module that CI builds. Namespace `org.telegram.messenger.regular`, `applicationId = APP_PACKAGE`. Flavors `afat` (all ABIs; the only one we build) and `bundleAfat`. A `variantFilter` ignores every build type other than `release` unless the flavor is `afat`, so `canary` works through `afat`.
- Other app modules (`TMessagesProj_AppStandalone`, `_AppHuawei`, `_AppHockeyApp`, `_AppTests`) exist and are configured by Gradle but never built by us.
- Build types: library has `debug`, `standalone`, `release` (and others); app has `debug`, `release`, and our `canary` (`initWith release`, `matchingFallbacks = ['release']`, same minify and signing as release, so a stable canary means a stable release). Output file is always `app.apk`; CI renames it.
- Signing: the app reads `TMessagesProj/config/release.keystore` with `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` from `gradle.properties`. CI overrides them with `ORG_GRADLE_PROJECT_*` environment variables and writes the keystore from a secret. The upstream keystore (password `android`) and the five upstream `google-services.json` files were deleted from the tree.
- The Google Services plugin is applied only in the app module. In the library it was removed because the library namespace `org.telegram.messenger` has no matching client in our `google-services.json` (run 88 failed with `No matching client found for package name 'org.telegram.messenger'`). No library code reads the resources that plugin generates.
- Native build ABIs follow the library, not the app: the app's `abiFilters` alone did not stop the library from compiling all four ABIs (51 minutes). Both modules read the Gradle property `ABI_FILTERS` (comma separated, default all four).
- Version text: `SettingsActivity.getVersionName`, `AndroidUtilities` and `ApplicationLoader` (the `buildVersion` log line) build `v<ver> (<code>) <label> <abi>`. The label used to be the literal `universal`; it is `BuildVars.BUILD_COMMIT` now. The ABI printed is the device's `Build.CPU_ABI`, not the APK's.

## 3. Our layer

Source of truth: `git diff --name-status 3de37c8cf HEAD` and `bash scripts/layer-check.sh` (must print `layer OK`).

### 3.1 Own code and tooling (cannot conflict with upstream)

| Path | Purpose |
|---|---|
| `TMessagesProj/src/main/java/app/lumine/LumineConfig.java` | own settings storage (`lumine_config` SharedPreferences, typed getters and setters). Every feature toggle goes here. Used by `LuminePushLog` and `PushConfig` (keys `push_*`). |
| `TMessagesProj/src/main/java/app/lumine/settings/*.java` | the luminegram settings screen: `LumineSettingsActivity` (content plus bottom bar), `LumineTabBar` (own view, no dependency on `MainTabsLayout`), `app/lumine/LumineStrings.java` (string lookup, see section 7), `LumineHomePage` (cards: App with version, build type and ABI; Notification (title) with two rows, Push Service and State (colored, from `PushStatus.health()`); tapping the card opens `LumineNotificationActivity`; Account; refreshes every 2 s while visible; tapping the push card copies the push report), `LumineGeneralPage` (a list of sections, each a row that opens its own page; today one row, Notification, with the value `<service> · <state>`), `LumineNotificationActivity` (own fragment with a back button: card Notification with Push Service dialog, State (tap copies the push report) and Battery optimization; card Details, shown for FCM and MicroG only, with Play Services, FCM token, Telegram registration, Last push and Last error; refreshes every 2 s while visible), `LumineCards` (card and row helpers), `LumineTabBar` drawn as a floating pill in the geometry of the main tab bar (56dp pill, 8dp margin, 48dp tabs, capsule behind the selected tab, theme keys `key_glass_*`; solid color, no blur, static vector icons instead of Lottie), `LumineComingSoonPage` (Appearance, Stealth and Experimental are placeholders). Strings in `res/values/lumine_strings.xml`, icons `drawable/lumine_tab_*.xml` and `lumine_settings_sparkle.xml`. |
| `TMessagesProj/src/main/java/app/lumine/push/*.java` | push layer. `PushConfig` (type 0 In-app, 1 FCM default, 2 UnifiedPush constant, 3 MicroG; `usesGoogle()` is true for FCM and MicroG; `createProvider()` returns the stock Google provider or an inert `InAppProvider` with `hasServices() == false`), `PushSwitcher` (leaving FCM or MicroG clears the token, resets `registeredForPush` and sends `account.unregisterDevice`; In-app toggles the keep-alive service and the push connection), `PushStatus` (`health(account)` is the single state source for Home, General and the Notification page: blocked, no Play Services, waiting for token or token failed, registering or registration failed, running; In-app is running or stopped; `services()`, `token()` and `registration(account)` feed the Details card; OS notification and battery checks), `LuminePushLog` (token, registration and last-push outcome plus a 40-line event log stored in `lumine_config`, independent of the app log switch; `report()` builds the text copied from the UI). `TMessagesProj_App/src/main/res/raw/lumine_keep.xml` keeps the Firebase config strings from resource shrinking. |
| `TMessagesProj/src/main/res/drawable/lumine_icon_foreground.xml` | black sparkle vector, used by the adaptive launcher icon (foreground and monochrome) |
| `.github/workflows/apk.yml` | workflow "Build APK" (section 5) |
| `scripts/ci/version.ts`, `post-card.ts`, `upload.sh` | APK names, Telegram card, upload |
| `scripts/layer-check.sh` | marker check (section 3.3) |
| `docs/base_luminegram.md` | this document (snapshot; the project copy is the one the owner uploads to the Claude project) |

New own code goes in the package `app.lumine.<feature>`, new resources get the prefix `lumine_`, new strings go in a file of ours (not in upstream's `strings.xml`), and each feature needs one settings entry and a toggle in `LumineConfig`.

### 3.2 Hooks and edits inside upstream files

Every row is a place where a merge can conflict.

| What | File | Marker |
|---|---|---|
| Application id override (the upstream line is left untouched, ours is appended at the end; the last definition wins) | `gradle.properties` | `APP_PACKAGE=com.lumine.gram` |
| API id/hash and commit from secrets and env; `getSecret` fails the build when a secret is missing | `TMessagesProj/build.gradle` | `getSecret("TELEGRAM_APP_ID")`, `"BUILD_COMMIT"` |
| Library ABI filter | `TMessagesProj/build.gradle` | `ABI_FILTERS` |
| Google Services plugin removed from the library | `TMessagesProj/build.gradle` | the string `com.google.gms.google-services` must be absent |
| `canary` build type, its source sets, ABI filter | `TMessagesProj_App/build.gradle` | `canary {`, `sourceSets.canary`, `abiFilterList` |
| API id/hash/commit read from BuildConfig (official id 4 and hash removed) | `messenger/BuildVars.java` | `BuildConfig.APP_ID`, `BuildConfig.APP_HASH`, `BuildConfig.BUILD_COMMIT` |
| Commit hash instead of `universal` | `ui/SettingsActivity.java`, `messenger/AndroidUtilities.java`, `messenger/ApplicationLoader.java` | `BuildVars.BUILD_COMMIT` |
| App name | `res/values/strings.xml` and 9 translations | `<string name="AppName">luminegram</string>` |
| Launcher icon: adaptive (API 26+) and PNG (API 23-25) | `res/mipmap-anydpi-v26/ic_launcher*.xml`, `res/mipmap-*/ic_launcher*.png` | `lumine_icon_foreground` |
| Small icon (contact sync, call and message notifications) | `res/drawable-*/ic_launcher_dr.webp` | none (binary) |
| Wordmark "luminegram" (generated from the bundled `rmediumitalic.ttf`; same viewport and size as the originals so the code that stretches them keeps working) | `res/drawable/telegram_logo.xml` (intro title), `telegram_logo_2.xml` (chat list title, stories row) | none (the file names are upstream's; the names are the marker) |
| Intro: sparkle texture replaces the plane, sphere is white | `res/drawable-*/intro_tg_plane.webp`, `ui/IntroActivity.java` | `paint.setColor(0xFFFFFFFF);` |
| Entry row "luminegram" in the main Settings (id 30, brand gradient, own sparkle icon) | `ui/SettingsActivity.java` | `import app.lumine.settings.LumineSettingsActivity;`, `presentSettingFragment(new LumineSettingsActivity());`, `R.drawable.lumine_settings_sparkle` |
| Push provider selection | `messenger/ApplicationLoader.java`, `messenger/GcmPushListenerService.java` | `app.lumine.push.PushConfig.createProvider`, `app.lumine.push.PushConfig.usesGoogle` (2 places in the service: `onMessageReceived`, `onNewToken`) |
| Push diagnostics | `messenger/PushListenerController.java`, `messenger/MessagesController.java` | `LuminePushLog.pushReceived`, `LuminePushLog.tokenRequested`, `LuminePushLog.tokenReceived`, `LuminePushLog.tokenFailed` (2 places), `LuminePushLog.registerResult` |
| Build type name for the Home card | `TMessagesProj/build.gradle`, `messenger/BuildVars.java` | `"BUILD_TYPE_NAME"` (from env `BUILD_TYPE`, default `local`), `BuildConfig.BUILD_TYPE_NAME` |
| Removed upstream secrets | `TMessagesProj/config/release.keystore`, five `google-services.json` | `layer-check.sh` fails if any keystore or `google-services.json` is tracked |

`IntroActivity.java` still imports `ThemeColors` (now unused); leave it, removing it only adds a conflict surface.

### 3.3 Layer check

`bash scripts/layer-check.sh` verifies the own files, every marker above, that the official API id and hash are gone, that no secret file is tracked and that the effective `APP_PACKAGE` (last definition in `gradle.properties`) is `com.lumine.gram`. Negative tests were run on 2026-11-07: removing a hook, restoring the official app id, reverting the package and tracking a `google-services.json` were each reported. When a new hook is added to an upstream file, add its marker in the same commit.

## 4. Syncing with upstream

How hard is it? On 2026-11-07, by simulation: our patch (`git diff --binary 3de37c8cf HEAD`) was applied with `git apply --3way` onto three older releases (13.1.0, 13.0.3, 13.0.1; the last one is 324 files and about 39,000 lines away). Every hunk applied except `gradle.properties` (the package line sat next to the version lines that change every release). That conflict source was removed afterwards by moving the override to the end of the file. The simulation measures text conflicts only; it cannot show a compile error when upstream renames something a hook calls, and the real procedure below has not run yet.

Procedure:
1. `git fetch upstream master`.
2. Merge up to the latest release commit (`git log upstream/master --grep='^update to' -1`), not necessarily the tip of `master`: the tip often carries merged pull requests that are not in a release yet.
3. `git merge <release commit> --no-commit --no-ff`, resolve, then `bash scripts/layer-check.sh` (must print `layer OK`).
4. Compare the upstream lines that affect the build (`TMessagesProj/build.gradle`, `TMessagesProj_App/build.gradle`, `gradle.properties`, NDK and CMake versions in the workflow) before accepting.
5. Commit with the default merge message, push, run a `canary` build, test on the device, and only then a `release` build.
6. Record the new sync point at the top of this document.

If upstream ever rewrites its history, use the tree-delta method described in `base_nagramxf.md` section 2.

## 5. CI

- `.github/workflows/apk.yml` ("Build APK", `workflow_dispatch` only): job `prepare` then `build` (matrix per ABI) then `post`. Inputs: `abi` (arm64-v8a, armeabi-v7a, all), `build_type` (`canary` default, `release`), `destination` (private, community). Pick `base-luminegram` in the "Run workflow" dropdown.
- Secrets used (names only): `TELEGRAM_API_ID`, `TELEGRAM_API_HASH` (also the upload API for the bot; passed to Gradle as `TELEGRAM_APP_ID` and `TELEGRAM_APP_HASH`), `GOOGLE_SERVICES_JSON` (written to `TMessagesProj_App/google-services.json`), `KEYSTORE_BASE64` (written to `TMessagesProj/config/release.keystore`), `KEYSTORE_PASSWORD`, `KEY_PASSWORD`, `TELEGRAM_BOT_TOKEN`; variable `TELEGRAM_DM_CHAT_ID`. Key alias `lumine`.
- Gradle task: `:TMessagesProj_App:assembleAfatCanary` or `assembleAfatRelease`; output `TMessagesProj_App/build/outputs/apk/afat/<canary|release>/app.apk`. `canary` is a real build type end to end (Gradle, folder, file name, card); the word `staging` does not exist in this base.
- File name: `luminegram-<ver>-<hash7>-<abi>-<type>.apk`, for example `luminegram-13.1.1-65b3ba2-arm64-v8a-canary.apk`. Card: title "luminegram Build Completed", table with Build Type (`canary` or `release`), Base (`13.1.1`) and Commit (hash in `<code>`, plain text, no link), then the APK.
- Toolchain: JDK 21, NDK `27.2.12479018`, CMake `3.22.1` (what the library asks for; `3.31.6` is still installed and unused), `platforms;android-36`, `build-tools;36.0.0`, `nasm`. ccache is enabled through `CMAKE_C_COMPILER_LAUNCHER` and `CMAKE_CXX_COMPILER_LAUNCHER` (6941 cacheable calls in run 88, so it works).
- Build history: run 88 (4 ABIs compiled, 51 min, failed at `processReleaseGoogleServices` for the library); the next run failed in 1 min with `No matching client found for package name 'org.telegram.messenger'`; after the plugin was removed from the library the build succeeded.
- The workflow file must exist in the branch picked in the "Run workflow" dropdown. The owner ran it from `base-luminegram` while the default branch is still `base-nagramxf`; keep the workflow on both if the default branch changes.
- Reading CI logs: the sandbox cannot download them (the log storage hosts are blocked; `api.github.com` answers with a redirect to a host that does not resolve). Ask the owner for the zip of the logs; inside it the build step is `Build <abi>/15_Build <abi>.txt` (search `What went wrong`). The annotations endpoint (`/check-runs/<job id>/annotations`) answers, but only with "exit code 1". A step that copies the Gradle error lines into annotations would let the sandbox read failures without the owner; not added yet.

## 6. Known gaps and next work (nothing below is verified or done)

Branding left over from upstream (order is my suggestion; item 1 was checked and needs no change):
1. Update check (checked 2026-11-07, nothing to change): `BuildVars.CHECK_UPDATES` is `true` but is not an active risk. `LaunchActivity.checkAppUpdate` returns at its first guard unless `ApplicationLoader.isStandaloneBuild()` or `isBetaBuild()` is true, forced or not, and both are `false` by default; only `TMessagesProj_AppStandalone` overrides them. Our app module `TMessagesProj_App` does not, so the app never sends `help.getAppUpdate`. Nothing was edited (a change would only add a conflict surface). `layer-check.sh` flags the case where an upstream merge adds `isStandalone` or `isBeta` to `ApplicationLoaderImpl` of our module. Not tested on a device (no way to observe a request that is never sent).
2. Official identities still in `BuildVars`: `SAFETYNET_KEY`, `GOOGLE_AUTH_CLIENT_ID`, `PLAYSTORE_APP_URL` (points at `org.telegram.messenger`), `SUPPORTS_PASSKEYS` (the upstream comment says passkeys only work for official app ids; set to false unless proven). Check every reader of each field before changing it.
3. Contact sync account type `org.telegram.messenger` (`res/xml/auth.xml`, `contacts.xml`, `sync_contacts.xml`, `ContactsController`): can collide with the official Telegram app if both are installed.
4. Small notification icon in the status bar is still the paper plane (`notification` drawables; `icon_plane` is now unused). Alternate launcher icons `icon_2` to `icon_6` in the app-icon picker are still Telegram's.
5. Text that names Telegram products or the app ("Telegram for Android" in Settings, Premium, Stars, Business, Help links, `Page1Title` and other intro texts) was left alone on purpose.
6. FCM: works (owner, 2026-11-08). Client hooks are in section 3.2 (push provider selection and diagnostics). The service account JSON is uploaded at my.telegram.org (Push credentials) and the CI secret `GOOGLE_SERVICES_JSON` holds the whole `google-services.json` of the same Firebase project (`lumine-gram`, package `com.lumine.gram`). Open: UnifiedPush has a constant but no UI entry and no provider (it falls back to the inert in-app provider); MicroG is the same Google provider as FCM, so the two choices behave the same until MicroG-specific handling exists. Switching the service shows a restart hint; whether a restart is actually required was not tested.
7. Plugin flavor and Python plugins: not applicable on this base.

## 7. Gotchas learned on this base

- String ids: `LocaleController.getString(R.string.X)` resolves through a table generated by `buildSrc` (`TelegramStringsTask`) that only reads `values/strings.xml`. A string in our own file shows `LOC_ERR:null` at runtime (seen on the device, 2026-11-07, build `26008ec`). Our strings go through `LumineStrings.get(R.string.X)`, which reads plain Android resources. Adding our file to the generator include list in `buildSrc` is the alternative; it was not done to keep upstream files untouched.
- `BUILD_TYPE_NAME` is `local` for a build outside CI (the env `BUILD_TYPE` is only set by the workflow); the Home card shows that value as is.
- `SharedConfig.pushStringStatus` is set to `__FIREBASE_GENERATING_SINCE_<ts>__` when a token is requested and is never cleared after success, so it must not be used to decide that a token is missing; the token itself (`SharedConfig.pushString`) is the signal. `PushStatus.summary()` did this wrong until the fix above.
- Every push hook is a one-line call into `app.lumine.push`; the guard `PushConfig.usesGoogle()` in `GcmPushListenerService` drops FCM messages and tokens when the selected service is not a Google one. A merge that rewrites `GcmPushListenerService` or `PushListenerController` must keep these hooks (`layer-check.sh` covers `createProvider`, `usesGoogle`, `tokenFailed`, `pushReceived` and `registerResult`; `tokenRequested` and `tokenReceived` are not in it yet).
- Upstream commit messages are not tags. Releases are the commits whose message starts with `update to`; use them as sync points.
- `git log -n N -- <file>` limits the number of commits that touch the file, not a window of N commits: do not use it to estimate how often a file changes (this gave a wrong "8 of 8" once). Use a range such as `<old>..<new> -- <file>`.
- `git push` from a shallow clone to a repo that does not have the commits is refused; this base was pushed from a full clone of upstream.
- The wordmark vectors are generated: `fontTools` reads `assets/fonts/rmediumitalic.ttf`, positions the glyphs of `luminegram`, flips Y and fits them into the original viewport (249x57 and 269x61), keeping the aspect ratio. Regenerating is cheap; the font file is part of the upstream tree.
- The launcher icon art is generated too: sparkle path `M12 3Q12.8 11.2 21 12Q12.8 12.8 12 21Q11.2 12.8 3 12Q11.2 11.2 12 3Z` plus the small one `M18 3Q18.3 5.7 21 6Q18.3 6.3 18 9Q17.7 6.3 15 6Q17.7 5.7 18 3Z`, scaled 58/24 and shifted 25 on a 108 viewport (adaptive), 60% of the circle width in the PNGs.
- The intro page is OpenGL: textures `intro_tg_plane` (plane) and a sphere bitmap drawn in `IntroActivity`, composed by native code. A texture replacement keeps the same canvas size on purpose.
