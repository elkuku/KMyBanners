# Erste Schritte

KMyBanners macht eine Webseite aus den Ingress-Missionsbannern, die du gemacht hast. Du machst in Ingress
Screenshots deiner Missionsmedaillen und teilst sie mit der App; sie findet die Banner auf
[Bannergress](https://bannergress.com) und veröffentlicht die Seite.

Es gibt keinen KMyBanners-Server: Die Seite liegt in einem Repository auf **deinem GitHub-Konto** und wird
mit GitHub Pages veröffentlicht, unter `https://<dein GitHub-Name>.github.io/<Name des Repositorys>/`
(`ingress-banners`, wenn du keinen anderen Namen wählst).
[So sieht eine aus](https://elkuku.github.io/ingress-banners/).

## Was du brauchst {#was-du-brauchst}

- Ein Android-Handy mit Android 8.0 oder neuer, mit Ingress.
- Ein [GitHub](https://github.com/signup)-Konto. Das kostenlose reicht.
- Ein [Bannergress](https://bannergress.com)-Konto, auf dem deine erledigten Banner markiert sind. Die
  App liest deine Erledigt-Liste, um deine Banner zu finden.

## Die App installieren {#die-app-installieren}

Lade die APK auf der Seite [Downloads](/de/downloads) herunter und öffne sie auf dem Handy. Android
fragt, ob dein Browser Apps installieren darf: Erlaube es.

## Mit GitHub anmelden {#mit-github-anmelden}

Die App meldet sich mit der *Geräte*-Anmeldung von GitHub an: Sie zeigt einen Code, und du bestätigst ihn
auf GitHub in deinem Browser. Dein GitHub-Passwort sieht die App nie.

<div class="shots">
  <img src="/screenshots/app/de/start-signin.webp" alt="Die App vor der Anmeldung">
  <img src="/screenshots/app/de/start-code.webp" alt="Die App zeigt den Code">
</div>

1. Tippe auf **Mit GitHub anmelden**. Die App zeigt einen Code wie `CC83-AB3B`.
2. Tippe auf **Code kopieren und GitHub öffnen**. Dein Browser öffnet `github.com/login/device`. Bist du
   dort nicht bei GitHub angemeldet, melde dich zuerst an.
3. GitHub zeigt das Konto, mit dem du angemeldet bist: Tippe auf **Continue** oder auf **Use a different
   account**.
4. Tippe den Code in die Felder und tippe auf **Continue**. Einfügen füllt oft nur das erste Feld, also
   tippe ihn ein.
5. GitHub zeigt, was **KMyBanners** möchte: *Public repositories* (öffentliche Repositorys). Tippe auf
   **Authorize**.
6. Nutzt du die Zwei-Faktor-Authentifizierung, fragt GitHub noch einmal nach deinem Code (**Confirm
   access**).
7. GitHub meldet *Congratulations, you're all set!*

<div class="shots">
  <img src="/screenshots/github/1-account.webp" alt="GitHub: Device Activation, das Konto">
  <img src="/screenshots/github/2-code.webp" alt="GitHub: der eingegebene Code">
  <img src="/screenshots/github/3-authorize.webp" alt="GitHub: Authorize KMyBanners">
  <img src="/screenshots/github/4-confirm.webp" alt="GitHub: Confirm access mit Zwei-Faktor-Authentifizierung">
  <img src="/screenshots/github/5-done.webp" alt="GitHub: Congratulations, you're all set">
</div>

Dann geh zurück in die App: Sie merkt die Anmeldung von selbst, innerhalb weniger Sekunden. Der Code gilt
15 Minuten; läuft er ab oder tippst du auf GitHub auf **Cancel**, fang neu an.

Die App braucht Zugriff auf deine *öffentlichen* Repositorys, um deine Seite anzulegen und deine
Screenshots hochzuladen. Sie ändert nur das Repository deiner Seite. Die Anmeldung bei GitHub gilt
8 Stunden, und die App erneuert sie von selbst: Du meldest dich nur einmal an.

## Deine Seite anlegen {#deine-seite-anlegen}

<img class="shot-phone right" src="/screenshots/app/de/start-create.webp" alt="Die Seite anlegen">

Gib deinen Agentennamen ein und, wenn du willst, einen anderen Namen für das Repository: Er ist Teil der
Adresse deiner Seite, die die App unter dem Feld zeigt. Der Name darf Buchstaben, Ziffern und `-` `_` `.`
enthalten; vorgegeben ist `ingress-banners`. Tippe auf **Meine Seite anlegen**: Die App legt das
öffentliche Repository auf deinem Konto an und schaltet GitHub Pages ein. Hast du schon ein Repository mit
diesem Namen, das keine Bannerseite ist, wähle einen anderen Namen.

Der Name `<dein GitHub-Name>.github.io` macht die Seite zur eigenen Seite deines Kontos, unter
`https://<dein GitHub-Name>.github.io/`, wenn du die nicht schon nutzt.

<div style="clear: both"></div>

## Deine neue Seite {#deine-neue-seite}

<img class="shot-phone right" src="/screenshots/app/de/start-empty.webp" alt="Eine neue Seite: noch nichts darauf">

Die Startseite zeigt oben deine Seite und darunter die Schritte. Auf einer neuen Seite ist noch nichts:

1. [Deine Bannergress-Erledigt-Liste hochladen](/de/guide/medals#deine-erledigt-liste).
2. [Screenshots deiner Missionsmedaillen teilen](/de/guide/medals#screenshots-machen).
3. [Die Medaillen prüfen, bei denen die App unsicher ist](/de/guide/check).

Sind alle drei erledigt, sieht es so aus, mit den Zahlen deiner Seite oben:

<img class="shot-phone" src="/screenshots/app/de/home.webp" alt="Die Startseite mit einer fertigen Seite">

## Einstellungen {#einstellungen}

**Einstellungen** (oben rechts) hat die Sprache der App (Deutsch, Englisch, Spanisch oder die des
Handys), hell oder dunkel, dein GitHub-Konto mit **Abmelden**, das Repository deiner Seite und die Version.

**Umbenennen** ändert den Namen des Repositorys, nach einer Rückfrage. Deine Seite zieht dann an die neue
Adresse um und wird dort neu erstellt. GitHub leitet die alte Adresse einer Seite nicht weiter: Sie
funktioniert nicht mehr, auch nicht in Links, die du geteilt hast. Die App findet die Seite am Topic
`kmybanners`, das sie dem Repository gibt, auch auf einem anderen Handy oder nach einer Umbenennung auf
der Website von GitHub.

<img class="shot-phone" src="/screenshots/app/de/settings.webp" alt="Die Einstellungen">
