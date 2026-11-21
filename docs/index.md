---
layout: home

hero:
  name: KMyBanners
  text: Your Ingress banners on a page of your own
  tagline: Take screenshots of your mission medals in Ingress and share them with the Android app. It finds your banners on Bannergress and publishes a page with a map of where you did them, on your own GitHub account, free.
  image:
    src: /logo.svg
    alt: KMyBanners
  actions:
    - theme: brand
      text: Get started
      link: /guide/
    - theme: alt
      text: Download the app
      link: /downloads
    - theme: alt
      text: See an example page
      link: https://elkuku.github.io/nikp3h-ingress-banners/

features:
  - icon: 📸
    title: From screenshots
    details: Scroll through your mission medals in Ingress, take screenshots and share them with the app. It puts them together into one long picture.
    link: /guide/medals#take-the-screenshots
  - icon: 🔎
    title: Matched with Bannergress
    details: Every row of medals is compared with the banners on your Bannergress done list, picture by picture.
    link: /guide/medals#your-done-list
  - icon: ✅
    title: You have the last word
    details: Rows the app isn't sure about are marked to check. Pick the banner, paste a Bannergress link or mark them as single missions.
    link: /guide/check
  - icon: 🗺️
    title: A page with a map
    details: All your banners with their medals, a map of the start points, countries, a search and filters.
    link: /guide/medals#your-page
  - icon: 🐙
    title: On your GitHub account
    details: No server and no account of ours. Your page is a repository on your GitHub account, published with GitHub Pages.
    link: /guide/privacy
  - icon: 🌍
    title: English, German, Spanish
    details: The app and this guide in three languages, with a light and a dark look.
    link: /guide/#settings
---

<div class="screens">
  <img src="/screenshots/app/en/start-empty.webp" alt="A new page: nothing on it yet">
  <img src="/screenshots/app/en/home.webp" alt="The front page with a finished page">
  <img src="/screenshots/app/en/check.webp" alt="Checking a row of medals">
  <img src="/screenshots/app/en/my-banners.webp" alt="My banners">
</div>

<div class="page-shot">
  <img src="/screenshots/web/page.webp" alt="A banner page: the mission medals, a map of the start points and every banner">
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
