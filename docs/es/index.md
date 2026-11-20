---
layout: home

hero:
  name: KMyBanners
  text: Tus banners de Ingress en una página propia
  tagline: Haz capturas de tus medallas de misión en Ingress y compártelas con la app de Android. Encuentra tus banners en Bannergress y publica una página con un mapa de dónde los hiciste, en tu propia cuenta de GitHub, gratis.
  image:
    src: /logo.svg
    alt: KMyBanners
  actions:
    - theme: brand
      text: Empezar
      link: /es/guide/
    - theme: alt
      text: Descargar la app
      link: /es/downloads
    - theme: alt
      text: Ver una página de ejemplo
      link: https://elkuku.github.io/ingress-banners/

features:
  - icon: 📸
    title: A partir de capturas
    details: Desplázate por tus medallas de misión en Ingress, haz capturas y compártelas con la app. Las une en una sola imagen larga.
    link: /es/guide/medals#haz-las-capturas
  - icon: 🔎
    title: Comparadas con Bannergress
    details: Cada fila de medallas se compara, imagen por imagen, con los banners de tu lista de completados de Bannergress.
    link: /es/guide/medals#tu-lista-de-completados
  - icon: ✅
    title: Tú decides
    details: Las filas de las que la app no está segura quedan por revisar. Elige el banner, pega un enlace de Bannergress o márcalas como misiones sueltas.
    link: /es/guide/check
  - icon: 🗺️
    title: Una página con mapa
    details: Todos tus banners con sus medallas, un mapa de los puntos de inicio, los países, una búsqueda y filtros.
    link: /es/guide/medals#tu-pagina
  - icon: 🐙
    title: En tu cuenta de GitHub
    details: Sin servidor y sin cuenta nuestra. Tu página es un repositorio en tu cuenta de GitHub, publicado con GitHub Pages.
    link: /es/guide/privacy
  - icon: 🌍
    title: Español, inglés, alemán
    details: La app y esta guía en tres idiomas, con tema claro y oscuro.
    link: /es/guide/#ajustes
---

<div class="screens">
  <img src="/screenshots/app/es/start-empty.webp" alt="Una página nueva: aún no hay nada">
  <img src="/screenshots/app/es/home.webp" alt="La página principal con una página terminada">
  <img src="/screenshots/app/es/check.webp" alt="Revisar una fila de medallas">
  <img src="/screenshots/app/es/my-banners.webp" alt="Mis banners">
</div>

<div class="page-shot">
  <img src="/screenshots/web/page.webp" alt="Una página de banners: las medallas de misión, un mapa de los puntos de inicio y todos los banners">
</div>

<style>
.screens {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  max-width: 1152px;
  margin: 48px auto 0;
  padding: 0 24px;
}
.screens img, .page-shot img {
  border-radius: 12px;
  border: 1px solid var(--vp-c-divider);
}
.page-shot {
  max-width: 1152px;
  margin: 24px auto 0;
  padding: 0 24px;
}
@media (max-width: 768px) {
  .screens { grid-template-columns: repeat(2, 1fr); }
}
</style>
