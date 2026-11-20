package dev.elkuku.kmybanners

/** The agent's page repository: its name (chosen when the page is made, can be changed later) and address. */
object PageRepo {
    /** The name the app suggests. */
    const val DEFAULT = "ingress-banners"

    /** The topic the app gives the repo: how it finds the page again, whatever its name (new phone, a rename). */
    const val TOPIC = "kmybanners"

    private val NAME = Regex("[A-Za-z0-9._-]{1,100}")

    /** A name GitHub takes as it is (letters, digits, - _ .; it would change other characters). */
    fun valid(name: String): Boolean = NAME.matches(name) && name != "." && name != ".." && !name.endsWith(".git")

    /**
     * The page's address on GitHub Pages: https://<login>.github.io/<name>/, or the account's own site for the
     * name <login>.github.io (lower case, as GitHub Pages).
     */
    fun pageUrl(login: String, name: String): String {
        val host = "${login.lowercase()}.github.io"
        return if (name.equals(host, ignoreCase = true)) "https://$host/" else "https://$host/$name/"
    }
}
