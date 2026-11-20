---
title: Descargas
---

<script setup>
import { data } from '../downloads.data'
</script>

# Descargas

## La app {#la-app}

La app para Android necesita Android 8.0 o más reciente.

| | |
|---|---|
| **KMyBanners** | La app publicada: descarga el APK de la [última versión](https://github.com/elkuku/KMyBanners/releases). |
| **KMyBanners Dev** | <a href="/KMyBanners/kmybanners-dev.apk" download>kmybanners-dev.apk</a><span v-if="data.devApkVersion"> · {{ data.devApkVersion }}</span><span v-if="data.devApkSize"> · {{ data.devApkSize }}</span><br>Los últimos cambios en main, compilada en cada push. Se instala junto a la app publicada, con su propio icono. |

Abre el APK en tu teléfono y permite instalar apps desde tu navegador o gestor de archivos cuando Android
lo pida. Luego: [Primeros pasos](/es/guide/).

## Proyecto {#proyecto}

| | |
|---|---|
| **App** | [github.com/elkuku/KMyBanners](https://github.com/elkuku/KMyBanners) |
| **Creación de la página** | [github.com/elkuku/KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) |
| **Plantilla de la página** | [github.com/elkuku/KMyBanners-template](https://github.com/elkuku/KMyBanners-template) |

## Tecnología {#tecnologia}

| | |
|---|---|
| **App** | Versión {{ data.app.version }} · Kotlin {{ data.app.kotlin }} · Jetpack Compose (BOM {{ data.app.composeBom }}) · Material 3 |
| **Bibliotecas** | Ktor {{ data.app.ktor }} · kotlinx.serialization · Coil {{ data.app.coil }} |
| **Android** | minSdk {{ data.app.minSdk }} (Android 8.0) · targetSdk {{ data.app.targetSdk }} · Android Gradle plugin {{ data.app.agp }} |
| **Código de la app** | {{ data.app.sourceFiles }} archivos fuente de Kotlin · {{ data.app.testFiles }} archivos de pruebas |
| **Creación de la página** | Python · Pillow · GitHub Actions · GitHub Pages |
| **Documentación** | [VitePress](https://vitepress.dev) |

<p class="build-date">Compilada {{ data.buildDate }}</p>

<style>
.build-date { color: var(--vp-c-text-3); font-size: 0.85em; }
</style>
