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

## Build

```sh
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest assembleDebug
```

Needs `local.properties` with `sdk.dir`. The GitHub OAuth App's client id is in `app/build.gradle.kts`
(override with the Gradle property `github.clientId`).
