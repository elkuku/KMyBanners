# KMyBanners (Android)

The app that gives an Ingress agent a web page of their mission banners: their mission medals from
Ingress, matched with [Bannergress](https://bannergress.com), with a map of where each banner starts.

1. **Sign in with GitHub** (device flow): the app creates `<you>/ingress-banners` from
   [KMyBanners-template](https://github.com/elkuku/KMyBanners-template) and turns on GitHub Pages.
2. **Sign in to Bannergress** and upload your done list (`done.json`).
3. In Ingress, take screenshots of your mission medals while scrolling, and **share them to the app**:
   it uploads them in one commit to `inbox/`.
4. The page's workflow ([KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool)) stitches the
   screenshots, matches the medals with the done list and publishes the page at
   `https://<you>.github.io/ingress-banners/`.

## Download

Beta versions: the APK of the [latest release](https://github.com/elkuku/KMyBanners/releases) (Android 8.0 or
newer). Open it on your phone and allow installing apps from your browser when Android asks.

## Release

Set `versionName` (and `versionCode`) in `app/build.gradle.kts`, then push a tag `v<versionName>`: the
release workflow builds the APK, signs it with the release key (repo secrets `RELEASE_KEYSTORE_BASE64`,
`RELEASE_KEYSTORE_PASSWORD`) and publishes it as a GitHub Release. Locally, `release.*` in `local.properties`
signs `assembleRelease`; the debug build (`dev.elkuku.kmybanners.debug`, "KMyBanners Debug") installs next
to it.

## Build

```sh
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest assembleDebug
```

Needs `local.properties` with `sdk.dir`. The GitHub OAuth App's client id is in `app/build.gradle.kts`
(override with the Gradle property `github.clientId`).
