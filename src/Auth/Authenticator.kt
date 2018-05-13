package Auth

/**
 * Created by beepbeat/holladiewal on 13.11.2017.
 */
import secret
import java.io.IOException
import java.net.URI
import java.net.URL

/*The MIT License (MIT)

        Copyright (c) 2015 Matthew J. Bell

        Permission is hereby granted, free of charge, to any person obtaining a copy
        of this software and associated documentation files (the "Software"), to deal
        in the Software without restriction, including without limitation the rights
        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
        copies of the Software, and to permit persons to whom the Software is
        furnished to do so, subject to the following conditions:

        The above copyright notice and this permission notice shall be included in all
        copies or substantial portions of the Software.

        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
        SOFTWARE.
*/
/**
 * The authenticator object allows a user to authenticate with the Twitch.tv servers.
 *
 * @author Matthew Bell
 */
class Authenticator(private val twitchBaseUrl: String // Base twitch api url
) {
    private val listenPort: Int = 13370 // The port to listen for the authentication callback on
    val clientId: String? = secret.clientID
    val redirectUri: URI? = URI.create("https://localhost:13370")

    var accessToken: String? = null
    /**
     * Get the authentication error if it failed.
     *
     * @return `AuthenticationError` if authentication failed. `null` otherwise
     */
    var authenticationError: AuthenticationError? = null
        private set

    /**
     * Listens for callback from Twitch server with the access token.
     * `getAuthenticationUrl()` must be called prior to this function!
     *
     * Allows for custom authorize pages. `null` can be passed to use the default page.
     *
     * @param authUrl    the URL to a custom authorize page.
     * @param successUrl the URL to a custom successful authentication page.
     * @param failUrl    the URL to a custom failed authentication page.
     * @return
     */
    @JvmOverloads
    fun awaitAccessToken(authUrl: URL? = null, successUrl: URL? = null, failUrl: URL? = null): Boolean {
        if (clientId == null || redirectUri == null) return false
        val url = URL("https://localhost:13370")
        val server = AuthenticationCallbackServer(listenPort, url, url, url)
        try {
            server.start()
        } catch (e: IOException) {
            authenticationError = AuthenticationError("JavaException", e.toString())
            return false
        }

        if (server.hasAuthenticationError() || server.accessToken == null) {
            authenticationError = server.authenticationError
            return false
        }

        accessToken = server.accessToken
        return true
    }

    /**
     * Check if an access token has been received.
     *
     * @return `true` if an access token has been received; `false` otherwise
     */
    fun hasAccessToken(): Boolean {
        return accessToken != null
    }

    /**
     * Check if there was an authentication error.
     *
     * @return `true` if an error exists; `false` otherwise
     */
    fun hasAuthenticationError(): Boolean {
        return authenticationError != null
    }

}
/**
 * Listens for callback from Twitch server with the access token.
 * `getAuthenticationUrl()` must be called prior to this function!
 *
 * @return `true` if access token was received, `false` otherwise
 */// Use default pages
