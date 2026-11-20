---
title: Downloads
---

<script setup>
import { data } from '../downloads.data'
</script>

# Downloads

## Die App {#die-app}

Die Android-App braucht Android 8.0 oder neuer.

| | |
|---|---|
| **KMyBanners** | Die Release-App: Lade die APK vom [neuesten Release](https://github.com/elkuku/KMyBanners/releases) herunter. |
| **KMyBanners Dev** | <a href="/KMyBanners/kmybanners-dev.apk" download>kmybanners-dev.apk</a><span v-if="data.devApkVersion"> · {{ data.devApkVersion }}</span><span v-if="data.devApkSize"> · {{ data.devApkSize }}</span><br>Die neuesten Änderungen auf main, bei jedem Push gebaut. Sie wird neben der Release-App installiert, mit eigenem Symbol. |

Öffne die APK auf dem Handy und erlaube das Installieren von Apps aus deinem Browser oder Dateimanager,
wenn Android fragt. Dann: [Erste Schritte](/de/guide/).

## Projekt {#projekt}

| | |
|---|---|
| **App** | [github.com/elkuku/KMyBanners](https://github.com/elkuku/KMyBanners) |
| **Erstellen der Seite** | [github.com/elkuku/KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) |
| **Vorlage der Seite** | [github.com/elkuku/KMyBanners-template](https://github.com/elkuku/KMyBanners-template) |

## Technik {#technik}

| | |
|---|---|
| **App** | Version {{ data.app.version }} · Kotlin {{ data.app.kotlin }} · Jetpack Compose (BOM {{ data.app.composeBom }}) · Material 3 |
| **Bibliotheken** | Ktor {{ data.app.ktor }} · kotlinx.serialization · Coil {{ data.app.coil }} |
| **Android** | minSdk {{ data.app.minSdk }} (Android 8.0) · targetSdk {{ data.app.targetSdk }} · Android Gradle Plugin {{ data.app.agp }} |
| **Code der App** | {{ data.app.sourceFiles }} Kotlin-Quelldateien · {{ data.app.testFiles }} Testdateien |
| **Erstellen der Seite** | Python · Pillow · GitHub Actions · GitHub Pages |
| **Doku** | [VitePress](https://vitepress.dev) |

<p class="build-date">Erstellt {{ data.buildDate }}</p>

<style>
.build-date { color: var(--vp-c-text-3); font-size: 0.85em; }
</style>
