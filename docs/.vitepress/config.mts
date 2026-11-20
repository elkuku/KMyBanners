import { defineConfig, type DefaultTheme } from 'vitepress'

/** The nav and sidebars of a language; `prefix` is its folder, e.g. '/de'. */
function themeConfig(prefix: string, t: {
  guide: string
  developers: string
  downloads: string
  gettingStarted: string
  medals: string
  check: string
  privacy: string
  developerGuide: string
}): DefaultTheme.Config {
  return {
    nav: [
      { text: t.guide, link: `${prefix}/guide/`, activeMatch: `${prefix}/guide/` },
      { text: t.developers, link: `${prefix}/developers/`, activeMatch: `${prefix}/developers/` },
      { text: t.downloads, link: `${prefix}/downloads` },
    ],

    sidebar: {
      [`${prefix}/guide/`]: [
        {
          text: t.guide,
          items: [
            { text: t.gettingStarted, link: `${prefix}/guide/` },
            { text: t.medals, link: `${prefix}/guide/medals` },
            { text: t.check, link: `${prefix}/guide/check` },
            { text: t.privacy, link: `${prefix}/guide/privacy` },
          ],
        },
      ],
      [`${prefix}/developers/`]: [
        {
          text: t.developers,
          items: [{ text: t.developerGuide, link: `${prefix}/developers/` }],
        },
      ],
    },
  }
}

// Published on GitHub Pages at https://elkuku.github.io/KMyBanners/ with the dev APK next to it
// (see .github/workflows/dev-build.yml)
export default defineConfig({
  title: 'KMyBanners',
  base: '/KMyBanners/',
  cleanUrls: true,
  lastUpdated: true,
  head: [['link', { rel: 'icon', type: 'image/svg+xml', href: '/KMyBanners/logo.svg' }]],
  srcExclude: ['scripts/**'],
  // Added by the workflow after the build
  ignoreDeadLinks: [/^\/kmybanners-dev\.apk$/],

  // English at the root, German in de/ and Spanish in es/, with the same pages in each
  locales: {
    root: {
      label: 'English',
      lang: 'en-US',
      description: 'Your Ingress mission banners on a web page of your own, made from screenshots of your mission medals.',
      themeConfig: {
        ...themeConfig('', {
          guide: 'Guide',
          developers: 'Developers',
          downloads: 'Downloads',
          gettingStarted: 'Getting started',
          medals: 'Your medals and done list',
          check: 'Checking your medals',
          privacy: 'Privacy',
          developerGuide: 'How it works',
        }),
        footer: { message: 'KMyBanners is not affiliated with Niantic Spatial, Ingress or Bannergress.' },
      },
    },
    de: {
      label: 'Deutsch',
      lang: 'de-DE',
      description: 'Deine Ingress-Missionsbanner auf einer eigenen Webseite, gemacht aus Screenshots deiner Missionsmedaillen.',
      themeConfig: {
        ...themeConfig('/de', {
          guide: 'Anleitung',
          developers: 'Entwickler',
          downloads: 'Downloads',
          gettingStarted: 'Erste Schritte',
          medals: 'Medaillen und Erledigt-Liste',
          check: 'Medaillen prüfen',
          privacy: 'Datenschutz',
          developerGuide: 'So funktioniert es',
        }),
        footer: { message: 'KMyBanners steht in keiner Verbindung zu Niantic Spatial, Ingress oder Bannergress.' },
        editLink: {
          pattern: 'https://github.com/elkuku/KMyBanners/edit/main/docs/:path',
          text: 'Diese Seite auf GitHub bearbeiten',
        },
        outline: { label: 'Auf dieser Seite' },
        docFooter: { prev: 'Vorherige Seite', next: 'Nächste Seite' },
        lastUpdated: { text: 'Zuletzt aktualisiert' },
        langMenuLabel: 'Sprache ändern',
        returnToTopLabel: 'Zurück nach oben',
        sidebarMenuLabel: 'Menü',
        darkModeSwitchLabel: 'Design',
        lightModeSwitchTitle: 'Zum hellen Design wechseln',
        darkModeSwitchTitle: 'Zum dunklen Design wechseln',
        skipToContentLabel: 'Zum Inhalt springen',
        notFound: {
          title: 'SEITE NICHT GEFUNDEN',
          quote: 'Diese Seite gibt es nicht.',
          linkLabel: 'Zur Startseite',
          linkText: 'Zur Startseite',
        },
      },
    },
    es: {
      label: 'Español',
      lang: 'es-ES',
      description: 'Tus banners de misiones de Ingress en una página web propia, hecha con capturas de tus medallas de misión.',
      themeConfig: {
        ...themeConfig('/es', {
          guide: 'Guía',
          developers: 'Desarrolladores',
          downloads: 'Descargas',
          gettingStarted: 'Primeros pasos',
          medals: 'Medallas y lista de completados',
          check: 'Revisar tus medallas',
          privacy: 'Privacidad',
          developerGuide: 'Cómo funciona',
        }),
        footer: { message: 'KMyBanners no está afiliado a Niantic Spatial, Ingress ni Bannergress.' },
        editLink: {
          pattern: 'https://github.com/elkuku/KMyBanners/edit/main/docs/:path',
          text: 'Editar esta página en GitHub',
        },
        outline: { label: 'En esta página' },
        docFooter: { prev: 'Página anterior', next: 'Página siguiente' },
        lastUpdated: { text: 'Última actualización' },
        langMenuLabel: 'Cambiar idioma',
        returnToTopLabel: 'Volver arriba',
        sidebarMenuLabel: 'Menú',
        darkModeSwitchLabel: 'Tema',
        lightModeSwitchTitle: 'Cambiar al tema claro',
        darkModeSwitchTitle: 'Cambiar al tema oscuro',
        skipToContentLabel: 'Saltar al contenido',
        notFound: {
          title: 'PÁGINA NO ENCONTRADA',
          quote: 'Esta página no existe.',
          linkLabel: 'Ir al inicio',
          linkText: 'Ir al inicio',
        },
      },
    },
  },

  themeConfig: {
    logo: '/logo.svg',

    socialLinks: [{ icon: 'github', link: 'https://github.com/elkuku/KMyBanners' }],

    editLink: {
      pattern: 'https://github.com/elkuku/KMyBanners/edit/main/docs/:path',
    },

    search: {
      provider: 'local',
      options: {
        locales: {
          de: {
            translations: {
              button: { buttonText: 'Suchen', buttonAriaLabel: 'Suchen' },
              modal: {
                displayDetails: 'Details anzeigen',
                resetButtonTitle: 'Suche zurücksetzen',
                backButtonTitle: 'Suche schließen',
                noResultsText: 'Keine Ergebnisse für',
                footer: { selectText: 'auswählen', navigateText: 'navigieren', closeText: 'schließen' },
              },
            },
          },
          es: {
            translations: {
              button: { buttonText: 'Buscar', buttonAriaLabel: 'Buscar' },
              modal: {
                displayDetails: 'Mostrar detalles',
                resetButtonTitle: 'Borrar la búsqueda',
                backButtonTitle: 'Cerrar la búsqueda',
                noResultsText: 'No hay resultados para',
                footer: { selectText: 'elegir', navigateText: 'navegar', closeText: 'cerrar' },
              },
            },
          },
        },
      },
    },
  },
})
