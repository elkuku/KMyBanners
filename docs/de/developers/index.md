# So funktioniert es

KMyBanners besteht aus drei Repositorys und keinem Server:

| Repository | Was |
|---|---|
| [KMyBanners](https://github.com/elkuku/KMyBanners) | Die Android-App und diese Doku (`docs/`) |
| [KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) | Das Erstellen der Seite: ein Python-Werkzeug (`kmybanners-tool`), HTML, CSS und JavaScript der Seite und der wiederverwendbare Workflow `build-page.yml` |
| [KMyBanners-template](https://github.com/elkuku/KMyBanners-template) | Die Vorlage für das Seiten-Repository jedes Agenten |

## Die Seite eines Agenten {#die-seite-eines-agenten}

Die App legt `<login>/<Name>` aus der Vorlage an (der Agent wählt den Namen, vorgegeben `ingress-banners`,
und kann ihn später in den Einstellungen ändern), gibt ihm das Topic `kmybanners`, an dem die App es wieder
findet, und schaltet GitHub Pages ein (aus Workflows).
Alles, was die App danach tut, ist ein Commit in dieses Repository:

| Datei | Geschrieben von | Was |
|---|---|---|
| `config.json` | der App | Der Agentenname, wann die Erledigt-Liste hochgeladen wurde |
| `inbox/<Zeit>/` | der App | Neue Screenshots (JPEG), ein Commit für alle |
| `captures/<Zeit>/` | dem Build | Die Screenshots jedes Builds, aus dem Eingang verschoben und aufbewahrt |
| `done.json` | der App | Die Bannergress-Erledigt-Liste |
| `corrections.json` | der App | Die Auswahl des Agenten für Medaillenreihen, nach den Bild-Fingerabdrücken der Reihen und ihrem Platz vom Ende der Liste (neue Medaillen kommen oben dazu, er bleibt also) |
| `capture/medals.jpg` | dem Build | Die zusammengesetzten Missionsmedaillen; neue Screenshots werden daran angesetzt |
| `notes.json` | von Hand | Optionale Notizen pro Banner |

Der Workflow `banner-page.yml` des Repositorys ruft
`elkuku/KMyBanners-tool/.github/workflows/build-page.yml@v1` auf. Er setzt die neuesten Screenshots
zusammen und setzt sie an die Medaillen davor an, wo drei Reihen hintereinander gleich sind (so braucht
ein Update Screenshots nur bis zu Medaillen, die schon da waren), verschiebt den Eingang nach `captures/`,
gleicht jede Medaillenreihe mit der Erledigt-Liste und den Korrekturen ab,
sucht für Reihen ohne Treffer Banner in der Nähe auf Bannergress und veröffentlicht die Seite. Die App
startet ihn mit `workflow_dispatch` und verfolgt den Lauf.

## Die App {#die-app}

Kotlin mit Jetpack Compose, Ktor und Coil.

- **GitHub:** der Device Flow der OAuth App *KMyBanners* (Scope `public_repo`, ablaufende Tokens mit
  Refresh). Hochladen über die Git Data API, so sind alle Screenshots ein Commit.
- **Bannergress:** Anmeldung in einer WebView mit dem Website-Client von Bannergress und PKCE; die
  Erledigt-Liste kommt aus der API von Bannergress.
- **Sprachen:** Englisch (`values/`), Deutsch (`values-de/`), Spanisch (`values-es/`).

```sh
cd KMyBanners
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest lintDebug assembleDebug
```

### Builds {#builds}

| Build | Paket | Signiert mit | Wo |
|---|---|---|---|
| debug | `dev.elkuku.kmybanners.dev` | dem Debug-Schlüssel | `assembleDebug`, CI-Artefakt |
| dev | `dev.elkuku.kmybanners.dev` | dem Dev-Schlüssel | `assembleDev`, [Downloads](/de/downloads), bei jedem Push auf main |
| release | `dev.elkuku.kmybanners` | dem Release-Schlüssel | ein Tag `v<versionName>`, GitHub Releases |

Debug- und Dev-Build heißen beide „KMyBanners Dev", sind mit DEV markiert und haben ein eigenes Symbol.
Sie haben verschiedene Schlüssel: Deinstalliere den einen, um den anderen zu installieren.

Für ein Release setze `versionName` und `versionCode` in `app/build.gradle.kts` und pushe ein Tag
`v<versionName>`. Die Schlüssel kommen aus den Repository-Secrets `RELEASE_KEYSTORE_*` und
`DEV_KEYSTORE_*`, lokal aus `release.*` und `dev.*` in `local.properties`.

## Das Erstellen der Seite {#das-erstellen-der-seite}

```sh
cd KMyBanners-tool
python3 -m venv .venv && .venv/bin/pip install -e . pytest
.venv/bin/kmybanners-tool site --repo <Agenten-Repo> -o _site --url https://…/
.venv/bin/python -m pytest
```

Die Tests mit echten Screenshots brauchen das private Repository KMyBanners-testdata daneben.

Änderungen erreichen die Seiten der Agenten, wenn das Tag `v1` auf sie verschoben wird.

## Diese Doku {#diese-doku}

VitePress in `docs/`, Englisch im Hauptordner und dieselben Seiten in `de/` und `es/`. Der
Dev-Build-Workflow erstellt sie zusammen mit der Dev-APK und veröffentlicht beides auf GitHub Pages.

```sh
cd docs
npm install
npm run dev
```

`npm run screenshots:app` macht die Screenshots der App auf einem Handy über adb, in allen drei Sprachen
(braucht Python mit Pillow). Die fertigen Ansichten kommen aus der angemeldeten Release-App. Der Anfang
(abgemeldet, der Anmeldecode, eine neue leere Seite, das erste Erstellen) kommt aus dem Demo-Modus des
Debug-Builds: Mit dem Intent-Extra `demo` gestartet (`ui/Demo.kt`), zeigt er sie als feste Ansichten.
Die GitHub-Seiten der Anmeldung (`public/screenshots/github/`) sind von Hand gemacht, ohne die
IP-Adresse, den Ort und die Organisationen, die GitHub zeigt.
