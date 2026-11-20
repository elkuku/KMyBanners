# KMyBanners (Android)

The app that gives an Ingress agent a web page of their mission banners: their mission medals from
Ingress, matched with [Bannergress](https://bannergress.com), with a map of where each banner starts.

**Docs:** [elkuku.github.io/KMyBanners](https://elkuku.github.io/KMyBanners/) (English, German, Spanish;
source in `docs/`, built with the dev APK by the dev-build workflow).

1. **Sign in with GitHub** (device flow): the app creates `<you>/<name>` (the agent
   chooses the name, `ingress-banners` by default; it can be renamed in the settings) from
   [KMyBanners-template](https://github.com/elkuku/KMyBanners-template) and turns on GitHub Pages.
2. **Sign in to Bannergress** and upload your done list (`done.json`).
3. In Ingress, take screenshots of your mission medals while scrolling, and **share them to the app**:
   it uploads them in one commit to `inbox/`.
4. The page's workflow ([KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool)) stitches the
   screenshots, matches the medals with the done list and publishes the page at
   `https://<you>.github.io/<name>/`. The app finds the repo again by its topic `kmybanners`.

## Download

Beta versions: the APK of the [latest release](https://github.com/elkuku/KMyBanners/releases) (Android 8.0 or
newer). Open it on your phone and allow installing apps from your browser when Android asks.

Dev build: [kmybanners-dev.apk](https://elkuku.github.io/KMyBanners/kmybanners-dev.apk), the latest changes on
main, built on every push. "KMyBanners Dev" (`dev.elkuku.kmybanners.dev`, marked DEV) installs next to the
release app.

## Release

Set `versionName` (and `versionCode`) in `app/build.gradle.kts`, then push a tag `v<versionName>`: the
release workflow builds the APK, signs it with the release key (repo secrets `RELEASE_KEYSTORE_BASE64`,
`RELEASE_KEYSTORE_PASSWORD`) and publishes it as a GitHub Release. Locally, `release.*` in `local.properties`
signs `assembleRelease`.

The dev build (`assembleDev`: a release build as "KMyBanners Dev", `dev.elkuku.kmybanners.dev`, marked DEV) is
signed with the dev key (repo secrets `DEV_KEYSTORE_BASE64`, `DEV_KEYSTORE_PASSWORD`; locally `dev.*` in
`local.properties`). The dev-build workflow publishes it on GitHub Pages on every push to main that changes the
app. The debug build has the same name and package, but a different key: uninstall one to install the other.

## Build

```sh
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest assembleDebug
```

Needs `local.properties` with `sdk.dir`. The GitHub OAuth App's client id is in `app/build.gradle.kts`
(override with the Gradle property `github.clientId`).
