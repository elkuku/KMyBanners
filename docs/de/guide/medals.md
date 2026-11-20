# Medaillen und Erledigt-Liste

## Deine Erledigt-Liste {#deine-erledigt-liste}

Die Seite findet deine Banner, indem sie deine Medaillen mit den Bildern der Banner auf deiner
Bannergress-Erledigt-Liste vergleicht. Die App braucht diese Liste also zuerst:

1. Tippe auf der Startseite auf **Bei Bannergress anmelden**. Die Anmeldung von Bannergress öffnet sich
   in der App; melde dich so an wie auf bannergress.com (zum Beispiel mit Google).
2. Tippe auf **Erledigt-Liste hochladen**.

Die Liste kommt in das Repository deiner Seite (`done.json`), und die Seite wird neu erstellt. Wenn du
auf Bannergress neue Banner als erledigt markiert hast, tippe auf **Neu hochladen**.

## Screenshots machen {#screenshots-machen}

<img class="shot-phone right" src="/screenshots/app/de/howto.webp" alt="So machst du die Screenshots, in der App">

1. Öffne in Ingress dein Agentenprofil, scrolle zu den Missionsmedaillen und tippe auf den Pfeil
   daneben. Die Liste aller deiner Missionsmedaillen öffnet sich.
2. Mach einen Screenshot. Scrolle etwa eine **halbe Bildschirmhöhe** weiter, warte, bis die Bilder
   geladen sind, und mach den nächsten. So weiter bis ans Ende der Liste.
3. Wähle in deiner Galerie alle Screenshots aus und teile sie mit **KMyBanners**.

Die App zeigt, wie viele Screenshots du geteilt hast. Tippe auf **Hochladen und Seite erstellen**.

<div class="shots">
  <img src="/screenshots/app/de/start-shared.webp" alt="Screenshots geteilt, bereit zum Hochladen">
  <img src="/screenshots/app/de/start-building.webp" alt="Die Seite wird erstellt">
</div>

Tipps:

- **Überlappung:** Jeder Screenshot muss einen Teil des vorigen zeigen, damit die App sie zusammensetzen
  kann. Eine halbe Bildschirmhöhe ist sicher.
- **Auf die Bilder warten:** Ingress lädt das Bild einer Medaille erst, wenn sie auf dem Bildschirm ist.
  Ein leerer Kreis (ein weißer Ring um Schwarz) heißt, sie ist noch nicht geladen: Warte kurz vor dem
  Screenshot.
- **Jedes Mal die ganze Liste:** Wenn du neue Medaillen hast, teile neue Screenshots der ganzen Liste.
  Sie ersetzen die alten.

## Das Erstellen der Seite {#das-erstellen}

<div style="clear: both"></div>

Nach dem Hochladen erstellt GitHub deine Seite: Es setzt die Screenshots zu einem Bild zusammen, teilt es
in Medaillenreihen und gleicht jede Reihe mit deiner Erledigt-Liste ab. Das dauert ein paar Minuten; die
App zeigt es an und sagt dir, wenn es fertig ist, auch wenn du sie zwischendurch schließt.

Schlägt es fehl, sagt die App das. **Auf GitHub ansehen** öffnet das Protokoll; **Nochmal versuchen**
startet es neu. Die häufigsten Gründe, im Protokoll auf Englisch:

| Meldung | Was tun |
|---|---|
| … *do not overlap* … | Die Screenshots überlappen nicht: zwischen den Screenshots weniger scrollen und sie in der richtigen Reihenfolge teilen. |
| … *had not loaded yet* … | Die Medaillen waren noch nicht geladen: vor jedem Screenshot warten, bis alle Bilder geladen sind. |

## Deine Seite {#deine-seite}

**Meine Seite öffnen** zeigt sie. Deine Seite hat:

- deine Missionsmedaillen wie in Ingress, die neuesten zuerst;
- eine Karte, wo jedes Banner beginnt, und die Länder;
- jedes Banner mit Medaillen, Missionen, Ort und einem Link zu Bannergress, mit einer Suche und Filtern
  (*Done*, *Done list only*, *To check*, *Single missions*). Die Seite selbst ist auf Englisch.

<img class="shot-web" src="/screenshots/web/page.webp" alt="Eine Bannerseite">

**Seite neu erstellen** erstellt sie ohne neue Screenshots, zum Beispiel nach einer Änderung auf
Bannergress. **GitHub** öffnet das Repository.
