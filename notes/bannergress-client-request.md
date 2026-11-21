# Request to the Bannergress admins: a Keycloak client for KMyBanners

Draft, not sent yet. Before sending: keep the AgentTools/kBannerGuider paragraph only if those apps
should get clients too, and send it where the Bannergress site points to (their Discord or GitHub).

---

**Subject: Request for a Keycloak client for KMyBanners (Android app)**

Hi Bannergress team,

I'm elkuku on GitHub (agent nikp3h). I'm building **KMyBanners** (https://github.com/elkuku/KMyBanners), a free,
open-source Android app that helps Ingress agents publish a page of the mission banners they've completed.
It's hosted on their own GitHub Pages; there's no server of mine in between.

The app reads the agent's Bannergress **done list** to match their mission medals to banners. For now it signs in
through a WebView using the `bannergress-website` client and catches the redirect to bannergress.com. That works,
but it borrows your website's client, and Google discourages signing in inside WebViews. I'd like to do it
properly, the way OpenBanners does with its own client.

Could you create a client in the `bannergress` realm with these settings?

- **Client ID:** `kmybanners`
- **Client type:** public (no client secret)
- **Flow:** standard flow (authorization code) only; no implicit flow, no direct access grants
- **PKCE:** required, method S256
- **Scope:** `openid`; the app keeps the refresh token so agents don't have to sign in each time
- **Valid redirect URIs:**
  - `dev.elkuku.kmybanners:/oauth` (release app)
  - `dev.elkuku.kmybanners.dev:/oauth` (dev build)

The app opens the sign-in page in a Chrome Custom Tab. It sends the tokens only to `login.bannergress.com` and
the Bannergress API, and stores them only on the phone. It only reads the done list; it doesn't change anything
on Bannergress.

I also maintain two other apps that read Bannergress lists, AgentTools and kBannerGuider. If you prefer, I can send
separate requests for them, or you could add their redirect URIs to this client.

Thanks a lot, and thanks for Bannergress!

elkuku / nikp3h
