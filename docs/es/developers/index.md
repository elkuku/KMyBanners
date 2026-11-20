# Cómo funciona

KMyBanners son tres repositorios y ningún servidor:

| Repositorio | Qué |
|---|---|
| [KMyBanners](https://github.com/elkuku/KMyBanners) | La app para Android y esta documentación (`docs/`) |
| [KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) | La creación de la página: una herramienta en Python (`kmybanners-tool`), el HTML, CSS y JavaScript de la página y el workflow reutilizable `build-page.yml` |
| [KMyBanners-template](https://github.com/elkuku/KMyBanners-template) | La plantilla del repositorio de la página de cada agente |

## La página de un agente {#la-pagina-de-un-agente}

La app crea `<login>/ingress-banners` a partir de la plantilla y activa GitHub Pages (desde workflows).
Todo lo que la app hace después es un commit en ese repositorio:

| Archivo | Escrito por | Qué |
|---|---|---|
| `config.json` | la app | El nombre de agente, cuándo se subió la lista de completados |
| `inbox/<hora>/` | la app | Capturas nuevas (JPEG), un commit para todas |
| `done.json` | la app | La lista de completados de Bannergress |
| `corrections.json` | la app | Las elecciones del agente para filas de medallas, por las huellas de imagen de las filas |
| `capture/medals.jpg` | la compilación | Las medallas de misión unidas |
| `notes.json` | a mano | Notas opcionales por banner |

El workflow `banner-page.yml` del repositorio llama a
`elkuku/KMyBanners-tool/.github/workflows/build-page.yml@v1`. Une las capturas más nuevas, vacía la
bandeja de entrada, compara cada fila de medallas con la lista de completados y las correcciones, busca
banners cercanos en Bannergress para las filas sin coincidencia y publica la página. La app lo inicia
con `workflow_dispatch` y sigue la ejecución.

## La app {#la-app}

Kotlin con Jetpack Compose, Ktor y Coil.

- **GitHub:** el device flow de la OAuth App *KMyBanners* (scope `public_repo`, tokens que caducan con
  refresh). Las subidas usan la Git Data API, así todas las capturas son un commit.
- **Bannergress:** inicio de sesión en un WebView con el cliente del sitio web de Bannergress y PKCE; la
  lista de completados viene de la API de Bannergress.
- **Idiomas:** inglés (`values/`), alemán (`values-de/`), español (`values-es/`).

```sh
cd KMyBanners
JAVA_HOME=~/.local/share/JetBrains/Toolbox/apps/android-studio/jbr ./gradlew testDebugUnitTest lintDebug assembleDebug
```

### Compilaciones {#compilaciones}

| Compilación | Paquete | Firmada con | Dónde |
|---|---|---|---|
| debug | `dev.elkuku.kmybanners.dev` | la clave de debug | `assembleDebug`, artefacto de CI |
| dev | `dev.elkuku.kmybanners.dev` | la clave dev | `assembleDev`, [Descargas](/es/downloads), en cada push a main |
| release | `dev.elkuku.kmybanners` | la clave de release | un tag `v<versionName>`, GitHub Releases |

Las compilaciones debug y dev se llaman las dos "KMyBanners Dev", están marcadas con DEV y tienen su
propio icono. Tienen claves distintas: desinstala una para instalar la otra.

Para publicar una versión, pon `versionName` y `versionCode` en `app/build.gradle.kts` y sube un tag
`v<versionName>`. Las claves vienen de los secrets del repositorio `RELEASE_KEYSTORE_*` y
`DEV_KEYSTORE_*`, en local de `release.*` y `dev.*` en `local.properties`.

## La creación de la página {#la-creacion-de-la-pagina}

```sh
cd KMyBanners-tool
python3 -m venv .venv && .venv/bin/pip install -e . pytest
.venv/bin/kmybanners-tool site --repo <repo del agente> -o _site --url https://…/
.venv/bin/python -m pytest
```

Las pruebas con capturas reales necesitan el repositorio privado KMyBanners-testdata al lado.

Los cambios llegan a las páginas de los agentes cuando el tag `v1` se mueve a ellos.

## Esta documentación {#esta-documentacion}

VitePress en `docs/`, inglés en la raíz y las mismas páginas en `de/` y `es/`. El workflow de la
compilación dev la crea junto con el APK dev y publica los dos en GitHub Pages.

```sh
cd docs
npm install
npm run dev
```

`npm run screenshots:app` hace las capturas de la app en un teléfono por adb, en los tres idiomas
(necesita Python con Pillow). Las pantallas terminadas vienen de la app publicada con la sesión iniciada.
El principio (sin sesión, el código de inicio de sesión, una página nueva vacía, la primera creación)
viene del modo demo de la compilación debug: iniciada con el extra de intent `demo` (`ui/Demo.kt`), las
muestra como pantallas fijas. Las páginas de GitHub del inicio de sesión (`public/screenshots/github/`)
están hechas a mano, sin la dirección IP, el lugar y las organizaciones que muestra GitHub.
