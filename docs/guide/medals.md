# Your medals and done list

## Your done list

The page finds your banners by comparing your medals with the pictures of the banners on your
Bannergress done list. So the app needs that list first:

1. On the front page, tap **Sign in to Bannergress**. Bannergress's sign-in opens in the app; sign in
   the way you do on bannergress.com (for example with Google).
2. Tap **Upload my done list**.

The list goes to your page's repository (`done.json`) and the page is built again. After you marked new
banners done on Bannergress, tap **Upload again**.

## Take the screenshots

<img class="shot-phone right" src="/screenshots/app/en/howto.webp" alt="How to take the screenshots, in the app">

1. In Ingress, open your agent profile, scroll down to the mission medals and tap the arrow next to
   them. The list of all your mission medals opens.
2. Take a screenshot. Scroll about **half a screen**, wait until the pictures have loaded, and take the
   next one. Go on down to the end of the list.
3. In your gallery, select all the screenshots and share them with **KMyBanners**.

The app shows how many screenshots you shared. Tap **Upload and build my page**.

<div class="shots">
  <img src="/screenshots/app/en/start-shared.webp" alt="Screenshots shared, ready to upload">
  <img src="/screenshots/app/en/start-building.webp" alt="The page is being built">
</div>

Tips:

- **Overlap:** each screenshot has to show part of the one before, so the app can join them. Half a
  screen is safe.
- **Wait for the pictures:** Ingress loads a medal's picture only when it is on the screen. An empty
  circle (a white ring around black) means it hasn't loaded yet: wait a moment before the screenshot.
- **Updates need only the new medals:** when you have new medals, take screenshots from the top of the
  list down to medals that are already on your page, and a few rows more. The app joins them onto your
  page's medals where they meet. Going down to the end of the list works too.

## The build

<div style="clear: both"></div>

After an upload, GitHub builds your page: it joins the screenshots into one picture, cuts it into rows
of medals and matches each row with your done list. That takes a few minutes; the app shows it and tells
you when it's done, also if you close it in between.

If the build fails, the app says so. **Show the build on GitHub** opens the log; **Try again** starts it
again. The most common reasons, in the log:

| Message | What to do |
|---|---|
| … *do not overlap* … | Scroll less between screenshots, and share them in order. |
| … *had not loaded yet* … | Wait until every picture has loaded before each screenshot. |

## Your page

**Open my page** shows it. Your page has:

- your mission medals as in Ingress, newest first;
- a map of where each banner starts, and the countries;
- every banner with its medals, missions, place and a link to Bannergress, with a search and filters
  (*Done*, *Done list only*, *Not on Bannergress*, *To check*, *Single missions*).

**Searching:** the search at the top (a title, place, Bannergress id or note) and the filters work on the
whole page. The table shows only the banners found and the map only their start points; in your mission
medals the other banners are darkened. **Only the banners found**, above the medals while you search or
filter, shows just their medals, one under the other. Click one to see it in the table and on the map.

<img class="shot-web" src="/screenshots/web/page.webp" alt="A banner page">

**Build my page again** builds it without new screenshots, for example after a change on Bannergress.
**GitHub** opens the repository.
