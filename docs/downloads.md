---
title: Downloads
---

<script setup>
import { data } from './downloads.data'
</script>

# Downloads

## The app

The Android app needs Android 8.0 or newer.

| | |
|---|---|
| **KMyBanners** | The release app: download the APK from the [latest release](https://github.com/elkuku/KMyBanners/releases). |
| **KMyBanners Dev** | <a href="/KMyBanners/kmybanners-dev.apk" download>kmybanners-dev.apk</a><span v-if="data.devApkVersion"> · {{ data.devApkVersion }}</span><span v-if="data.devApkSize"> · {{ data.devApkSize }}</span><br>The latest changes on main, built on every push. It installs next to the release app, with its own icon. |

Open the APK on your phone and allow installing apps from your browser or file manager when Android
asks. Then: [Getting started](/guide/).

## Project

| | |
|---|---|
| **App** | [github.com/elkuku/KMyBanners](https://github.com/elkuku/KMyBanners) |
| **Page build** | [github.com/elkuku/KMyBanners-tool](https://github.com/elkuku/KMyBanners-tool) |
| **Page template** | [github.com/elkuku/KMyBanners-template](https://github.com/elkuku/KMyBanners-template) |

## Tech stack

| | |
|---|---|
| **App** | Version {{ data.app.version }} · Kotlin {{ data.app.kotlin }} · Jetpack Compose (BOM {{ data.app.composeBom }}) · Material 3 |
| **App libraries** | Ktor {{ data.app.ktor }} · kotlinx.serialization · Coil {{ data.app.coil }} |
| **Android** | minSdk {{ data.app.minSdk }} (Android 8.0) · targetSdk {{ data.app.targetSdk }} · Android Gradle plugin {{ data.app.agp }} |
| **App code** | {{ data.app.sourceFiles }} Kotlin source files · {{ data.app.testFiles }} test files |
| **Page build** | Python · Pillow · GitHub Actions · GitHub Pages |
| **Docs** | [VitePress](https://vitepress.dev) |

<p class="build-date">Built {{ data.buildDate }}</p>

<style>
.build-date { color: var(--vp-c-text-3); font-size: 0.85em; }
</style>
