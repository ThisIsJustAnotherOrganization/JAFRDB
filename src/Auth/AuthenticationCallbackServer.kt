package Auth

/**
 * Created by beepbeat/holladiewal on 13.11.2017.
 */
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


import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.SocketException
import java.net.URL

class AuthenticationCallbackServer : AuthenticationListener {
    private val authPage: URL
    private val failurePage: URL
    private val successPage: URL
    private var port: Int = 13370
    private var serverSocket: ServerSocket? = null
    /**
     * Get the access token if one exists.
     *
     * @return Access token if it exists, `null` otherwise
     */
    var accessToken: String? = null
        private set // twitch.tv auth access token
    /**
     * Get the access scopes for the authenticated user.
     *
     * @return Array of [Scopes] that the authenticated user has
     */
    var accessScopes: Array<Scopes>? = null
        private set // scopes retrieves for access token
    /**
     * Get the auth error if it failed.
     *
     * @return Error string if auth failed. `null` otherwise
     */
    var authenticationError: AuthenticationError? = null
        private set

    /**
     * Check to see if server is running.
     *
     * @return `true` if server is running. `false` otherwise
     */
    val isRunning: Boolean
        get() = serverSocket != null

    /**
     * Constructor that will use default HTML views for output.
     *
     * @param port Network port to receive requests on
     */
    constructor(port: Int) {
        this.port = port
        // Load default pages
        authPage = javaClass.getResource(DEFAULT_AUTH_PAGE)
        failurePage = javaClass.getResource(DEFAULT_FAILURE_PAGE)
        successPage = javaClass.getResource(DEFAULT_SUCCESS_PAGE)
    }

    /**
     * Constructor with user specified HTML views to output.
     *
     * @param port        Network port to receive requests on
     * @param authPage    HTML page that twitch.tv will send the access_token to
     * @param failurePage HTML page that shows auth error to user
     * @param successPage HTML page that shows auth success to user
     */
    constructor(port: Int, authPage: URL?, failurePage: URL?, successPage: URL?) {
        this.port = port
        this.authPage = authPage ?: javaClass.getResource(DEFAULT_AUTH_PAGE)
        this.failurePage = failurePage ?: javaClass.getResource(DEFAULT_FAILURE_PAGE)
        this.successPage = successPage ?: javaClass.getResource(DEFAULT_SUCCESS_PAGE)
    }

    /**
     * Start the server and listen for auth callbacks from twitch.
     *
     * @throws IOException if an I/O error occurs while waiting for a connection.
     */
    @Throws(IOException::class)
    fun start() {
        // Establish the listen socket
        // For security reasons, the third parameter is set to not accept connections from outside the localhost
        serverSocket = ServerSocket(port, 0, InetAddress.getByName("127.0.0.1"))
        run()
    }

    @Throws(IOException::class)
    private fun run() {
        // Process HTTP service requests
        while (true) {
            try {
                // Listen for a TCP connection request
                val connectionSocket = serverSocket!!.accept()
                // Handle request
                val request = AuthenticationCallbackRequest(connectionSocket, authPage, failurePage, successPage)
                request.setAuthenticationListener(this)
                // Start thread
                val thread = Thread(request)
                thread.start()
            } catch (e: SocketException) {
                // Socket was closed by another thread
                break
            }

        }
    }

    /**
     * Stops the server.
     */
    fun stop() {
        if (serverSocket != null && !serverSocket!!.isClosed) {
            try {
                serverSocket!!.close()
            } catch (ignored: IOException) {
            } finally {
                serverSocket = null
            }
        }
    }

    override fun onAccessTokenReceived(token: String, vararg scopes: Scopes) {
        accessToken = token
        accessScopes = scopes.toMutableList().toTypedArray()
        stop() // Stop the server, we no longer need to process requests
    }

    override fun onAuthenticationError(error: String, description: String) {
        authenticationError = AuthenticationError(error, description)
        stop() // Stop the server
    }

    /**
     * Check if there was an authentication error
     *
     * @return `true` if an error exists, `false` otherwise
     */
    fun hasAuthenticationError(): Boolean {
        return authenticationError != null
    }

    companion object {


        val DEFAULT_AUTH_PAGE = "/authorize.html"

        /**
         * Default HTML page that shows auth error's to user
         */
        val DEFAULT_FAILURE_PAGE = "/authorize-failure.html"

        /**
         * Default HTML page that shows auth success to
         */
        val DEFAULT_SUCCESS_PAGE = "/authorize-success.html"
    }
}

