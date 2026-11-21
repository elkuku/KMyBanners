---
layout: home

hero:
  name: KMyBanners
  text: Deine Ingress-Banner auf einer eigenen Seite
  tagline: Mach in Ingress Screenshots deiner Missionsmedaillen und teile sie mit der Android-App. Sie findet deine Banner auf Bannergress und veröffentlicht eine Seite mit einer Karte, wo du sie gemacht hast – auf deinem eigenen GitHub-Konto, kostenlos.
  image:
    src: /logo.svg
    alt: KMyBanners
  actions:
    - theme: brand
      text: Los geht's
      link: /de/guide/
    - theme: alt
      text: App herunterladen
      link: /de/downloads
    - theme: alt
      text: Eine Beispielseite
      link: https://elkuku.github.io/nikp3h-ingress-banners/

features:
  - icon: 📸
    title: Aus Screenshots
    details: Scrolle in Ingress durch deine Missionsmedaillen, mach Screenshots und teile sie mit der App. Sie setzt sie zu einem langen Bild zusammen.
    link: /de/guide/medals#screenshots-machen
  - icon: 🔎
    title: Mit Bannergress abgeglichen
    details: Jede Medaillenreihe wird Bild für Bild mit den Bannern auf deiner Bannergress-Erledigt-Liste verglichen.
    link: /de/guide/medals#deine-erledigt-liste
  - icon: ✅
    title: Du hast das letzte Wort
    details: Reihen, bei denen die App unsicher ist, kommen zum Prüfen. Wähle das Banner, füge einen Bannergress-Link ein oder markiere sie als Einzelmissionen.
    link: /de/guide/check
  - icon: 🗺️
    title: Eine Seite mit Karte
    details: Alle deine Banner mit ihren Medaillen, eine Karte der Startpunkte, Länder, eine Suche und Filter.
    link: /de/guide/medals#deine-seite
  - icon: 🐙
    title: Auf deinem GitHub-Konto
    details: Kein Server und kein Konto bei uns. Deine Seite ist ein Repository auf deinem GitHub-Konto, veröffentlicht mit GitHub Pages.
    link: /de/guide/privacy
  - icon: 🌍
    title: Deutsch, Englisch, Spanisch
    details: Die App und diese Anleitung in drei Sprachen, hell und dunkel.
    link: /de/guide/#einstellungen
---

<div class="screens">
  <img src="/screenshots/app/de/start-empty.webp" alt="Eine neue Seite: noch nichts darauf">
  <img src="/screenshots/app/de/home.webp" alt="Die Startseite mit einer fertigen Seite">
  <img src="/screenshots/app/de/check.webp" alt="Eine Medaillenreihe prüfen">
  <img src="/screenshots/app/de/my-banners.webp" alt="Meine Banner">
</div>

<div class="page-shot">
  <img src="/screenshots/web/page.webp" alt="Eine Bannerseite: die Missionsmedaillen, eine Karte der Startpunkte und alle Banner">
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
