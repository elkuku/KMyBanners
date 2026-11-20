# How it works

KMyBanners is three repositories and no server:

| Repository | What |
|---|---|
| [KMyBanners](https://github.com/elkuku/KMyBanners) | The Android app, and these docs (`docs/`) |
| [KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) | The page build: a Python tool (`kmybanners-tool`), the page's HTML, CSS and JavaScript, and the reusable workflow `build-page.yml` |
| [KMyBanners-template](https://github.com/elkuku/KMyBanners-template) | What each agent's page repository is made from |

## An agent's page

The app creates `<login>/ingress-banners` from the template and turns on GitHub Pages (built by
workflows). Everything the app does after that is a commit to that repository:

| File | Written by | What |
|---|---|---|
| `config.json` | the app | The agent name, when the done list was uploaded |
| `inbox/<time>/` | the app | New screenshots (JPEG), one commit for all of them |
| `done.json` | the app | The Bannergress done list |
| `corrections.json` | the app | The agent's choices for medal rows, by the rows' picture fingerprints |
| `capture/medals.jpg` | the build | The stitched mission medals |
| `notes.json` | by hand | Optional notes per banner |

The repository's workflow `banner-page.yml` calls
`elkuku/KMyBanners-tool/.github/workflows/build-page.yml@v1`. It stitches the newest screenshots,
empties the inbox, matches each medal row with the done list and the corrections, looks up nearby
banners on Bannergress for rows it can't match, and deploys the page. The app starts it with
`workflow_dispatch` and follows the run.

## The app

Kotlin with Jetpack Compose, Ktor and Coil.

- **GitHub:** the device flow of the OAuth App *KMyBanners* (scope `public_repo`, expiring tokens with
  refresh). Uploads use the Git Data API, so all screenshots are one commit.
- **Bannergress:** sign-in in a WebView with Bannergress's own website client and PKCE; the done list
  comes from Bannergress's API.
- **Languages:** English (`values/`), German (`values-de/`), Spanish (`values-es/`).

```sh
cd KMyBanners
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest lintDebug assembleDebug
```

### Builds

| Build | Package | Signed with | Where |
|---|---|---|---|
| debug | `dev.elkuku.kmybanners.dev` | the debug key | `assembleDebug`, CI artifact |
| dev | `dev.elkuku.kmybanners.dev` | the dev key | `assembleDev`, [Downloads](/downloads), on every push to main |
| release | `dev.elkuku.kmybanners` | the release key | a tag `v<versionName>`, GitHub Releases |

The debug and dev builds are both "KMyBanners Dev", marked DEV and with their own icon. They have
different keys: uninstall one to install the other.

To release, set `versionName` and `versionCode` in `app/build.gradle.kts` and push a tag
`v<versionName>`. The keys come from the repository secrets `RELEASE_KEYSTORE_*` and
`DEV_KEYSTORE_*`, locally from `release.*` and `dev.*` in `local.properties`.

## The page build

```sh
cd KMyBanners-tool
python3 -m venv .venv && .venv/bin/pip install -e . pytest
.venv/bin/kmybanners-tool site --repo <agent repo> -o _site --url https://…/
.venv/bin/python -m pytest
```

The tests with real screenshots need the private repository KMyBanners-testdata next to it.

Changes reach agents' pages when the tag `v1` is moved to them.

## These docs

VitePress in `docs/`, English at the root and the same pages in `de/` and `es/`. The dev-build workflow
builds them together with the dev APK and publishes both on GitHub Pages.

```sh
cd docs
npm install
npm run dev
```

`npm run screenshots:app` takes the app's screenshots on a phone over adb, in all three languages
(needs Python with Pillow). The finished screens come from the release app, signed in. The start
(signed out, the sign-in code, a new empty page, the first build) comes from the debug build's demo
mode: started with the intent extra `demo` (`ui/Demo.kt`), it shows these as fixed screens. The GitHub
pages of the sign-in (`public/screenshots/github/`) are taken by hand, without the IP address, location
and organizations GitHub shows.
