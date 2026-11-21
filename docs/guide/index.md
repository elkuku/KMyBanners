# Getting started

KMyBanners makes a web page of the Ingress mission banners you have done. You take screenshots of your
mission medals in Ingress and share them with the app; it finds the banners on
[Bannergress](https://bannergress.com) and publishes the page.

There is no KMyBanners server: the page lives in a repository on **your GitHub account** and is published
with GitHub Pages, at `https://<your GitHub name>.github.io/<repository name>/` (`ingress-banners` unless you
choose another name).
[Here is one](https://elkuku.github.io/nikp3h-ingress-banners/), and the
[directory of agents' pages](https://elkuku.github.io/KMyBanners-directory/) lists them all; yours appears there
within a day, unless you turn that off in the [settings](#settings).

## What you need

- An Android phone with Android 8.0 or newer, with Ingress.
- A [GitHub](https://github.com/signup) account. The free one is enough.
- A [Bannergress](https://bannergress.com) account with your done banners marked. The app reads your
  done list to find your banners.

## Install the app

Download the APK from the [Downloads](/downloads) page and open it on your phone. Android asks whether
your browser may install apps: allow it.

## Sign in with GitHub

The app signs in with GitHub's *device* sign-in: it shows a code, and you confirm it on GitHub in your
browser. The app never sees your GitHub password.

<div class="shots">
  <img src="/screenshots/app/en/start-signin.webp" alt="The app before signing in">
  <img src="/screenshots/app/en/start-code.webp" alt="The app showing the code">
</div>

1. Tap **Sign in with GitHub**. The app shows a code like `CC83-AB3B`.
2. Tap **Copy the code and open GitHub**. Your browser opens `github.com/login/device`. If you aren't
   signed in to GitHub there, sign in first.
3. GitHub shows the account you are signed in with: tap **Continue**, or **Use a different account**.
4. Type the code into the boxes and tap **Continue**. Pasting it may only fill the first box, so type it.
5. GitHub shows what **KMyBanners** asks for: *Public repositories*. Tap **Authorize**.
6. If you use two-factor authentication, GitHub asks for your code once more (**Confirm access**).
7. GitHub says *Congratulations, you're all set!*

<div class="shots">
  <img src="/screenshots/github/1-account.webp" alt="GitHub: Device Activation, the account">
  <img src="/screenshots/github/2-code.webp" alt="GitHub: the code typed in">
  <img src="/screenshots/github/3-authorize.webp" alt="GitHub: Authorize KMyBanners">
  <img src="/screenshots/github/4-confirm.webp" alt="GitHub: Confirm access with two-factor authentication">
  <img src="/screenshots/github/5-done.webp" alt="GitHub: Congratulations, you're all set">
</div>

Then go back to the app: it notices the sign-in by itself, within a few seconds. The code is valid for
15 minutes; if it runs out, or you tap **Cancel** on GitHub, start again.

The app needs access to your *public* repositories to create your page and to upload your screenshots.
It only touches your page's repository. GitHub's sign-in lasts 8 hours and the app renews it
by itself, so you sign in only once.

## Create your page

<img class="shot-phone right" src="/screenshots/app/en/start-create.webp" alt="Creating the page">

Type your agent name and, if you like, another name for the repository: it is part of your page's
address, which the app shows below the field. The name may have letters, digits and `-` `_` `.`; the
default is `ingress-banners`. Tap **Create my page**: the app creates the public repository on your
account and turns on GitHub Pages. If you already have a repository with that name that isn't a banner
page, choose another name.

The name `<your GitHub name>.github.io` makes the page your account's own site, at
`https://<your GitHub name>.github.io/`, if you don't use that already.

<div style="clear: both"></div>

## Your new page

<img class="shot-phone right" src="/screenshots/app/en/start-empty.webp" alt="A new page: nothing on it yet">

The front page shows your page on top and the steps below it. On a new page, there's nothing on it yet:

1. [Upload your Bannergress done list](/guide/medals#your-done-list).
2. [Share screenshots of your mission medals](/guide/medals#take-the-screenshots).
3. [Check the medals the app isn't sure about](/guide/check).

When all three are done, it looks like this, with your page's numbers on top:

<img class="shot-phone" src="/screenshots/app/en/home.webp" alt="The front page with a finished page">

## Settings

**Settings** (top right) has the app's language (English, German, Spanish or the phone's), light or
dark, your GitHub account with **Sign out**, your page's repository, and the version.

**Rename** changes the repository's name, after asking. Your page then moves to the new address and is
built again there. GitHub doesn't forward the old address of a page: it stops working, also in links you
shared. The app finds the page by the topic `kmybanners` it gives the repository, also on another phone
or after a rename on GitHub's website.

**List my page in the directory** (on at first) decides whether the
[directory of agents' pages](https://elkuku.github.io/KMyBanners-directory/) shows your page. The directory is
updated once a day, so a change shows there by the next day.

<img class="shot-phone" src="/screenshots/app/en/settings.webp" alt="The settings">
