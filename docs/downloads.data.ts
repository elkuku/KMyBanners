import { existsSync, readdirSync, readFileSync, statSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

// The dev APK the Pages workflow built before the docs (copied into the site after the build); none in a
// local build unless you ran assembleDev. Its version comes from the workflow (DEV_APK_VERSION).
const root = fileURLToPath(new URL('../', import.meta.url))
const devApk = root + 'app/build/outputs/apk/dev/app-dev.apk'

/** A version from the version catalog (gradle/libs.versions.toml). */
function catalogVersion(catalog: string, name: string): string {
  return catalog.match(new RegExp(`^${name} = "([^"]+)"`, 'm'))?.[1] ?? ''
}

/** A setting from the app's build script, e.g. minSdk = 26. */
function buildSetting(script: string, name: string): string {
  return script.match(new RegExp(`\\b${name} = "?([^"\\s]+)"?`))?.[1] ?? ''
}

export interface Downloads {
  devApkVersion: string | null
  devApkSize: string | null
  app: {
    version: string
    kotlin: string
    agp: string
    composeBom: string
    ktor: string
    coil: string
    minSdk: string
    targetSdk: string
    sourceFiles: number
    testFiles: number
  }
  buildDate: string
}

export default {
  watch: [],
  load(): Downloads {
    const catalog = readFileSync(root + 'gradle/libs.versions.toml', 'utf8')
    const script = readFileSync(root + 'app/build.gradle.kts', 'utf8')
    const kotlin = (readdirSync(root + 'app/src', { recursive: true }) as string[]).filter((f) => f.endsWith('.kt'))
    const tests = kotlin.filter((f) => /^(test|androidTest)\//.test(f)).length

    return {
      devApkVersion: process.env.DEV_APK_VERSION || null,
      devApkSize: existsSync(devApk) ? (statSync(devApk).size / 1024 / 1024).toFixed(1) + ' MB' : null,
      app: {
        version: buildSetting(script, 'versionName'),
        kotlin: catalogVersion(catalog, 'kotlin'),
        agp: catalogVersion(catalog, 'agp'),
        composeBom: catalogVersion(catalog, 'composeBom'),
        ktor: catalogVersion(catalog, 'ktor'),
        coil: catalogVersion(catalog, 'coil'),
        minSdk: buildSetting(script, 'minSdk'),
        targetSdk: buildSetting(script, 'targetSdk'),
        sourceFiles: kotlin.length - tests,
        testFiles: tests,
      },
      buildDate: new Date().toISOString().slice(0, 16).replace('T', ' ') + ' UTC',
    }
  },
}
