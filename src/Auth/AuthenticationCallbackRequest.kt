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


import config
import entries
import java.io.*
import java.net.Socket
import java.net.URL
import java.net.URLDecoder
import java.util.*

class AuthenticationCallbackRequest
/**
 * Construct the request and specify which HTML files to server.
 *
 * @param socket      Connection socket of the request
 * @param authPage    HTML page that twitch.tv will send the access_token to
 * @param failurePage HTML page that shows auth error to user
 * @param successPage HTML page that shows auth success to user
 */
(private val socket: Socket, private val authPage: URL, private val failurePage: URL, private val successPage: URL) : Runnable {

    private var authenticationListener: AuthenticationListener? = null // Will receive auth callbacks

    fun setAuthenticationListener(receiver: AuthenticationListener) {
        this.authenticationListener = receiver
    }

    override fun run() {
        try {
            processRequest()
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }

    }

    /**
     * Process the HTTP request and send out correct page.
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun processRequest() {
        // Get a reference to the socket's input and output streams.
        val `is` = socket.getInputStream()
        val os = DataOutputStream(socket.getOutputStream())

        // Set up input stream filters.
        val br = BufferedReader(InputStreamReader(socket.getInputStream()))

        // Get the request line of the HTTP request message.
        val requestLine = br.readLine()

        // Store the request line for debugging.
        //String rawRequest = "\n" + requestLine;

        // Read the header lines.
        var headerLine: String = ""
        while (headerLine.isNotEmpty()) {
            //rawRequest += headerLine + "\n";
            headerLine = br.readLine()
        }

        // DEBUG: Print request
        //System.out.println(rawRequest);

        // Parse the request line.
        val tokens = StringTokenizer(requestLine)
        val requestMethod = tokens.nextToken()  // Request method, which should be "GET"
        val requestFilename = tokens.nextToken()
        val queryParams = extractQueryParams(requestFilename)

        // If we have the token, send the success page
        val accessToken = queryParams["code"]
        config.varMap["${entries.token}"] = accessToken.toString()
        var scopes = arrayOfNulls<String>(0)
        if (queryParams.containsKey("scope")) {
            scopes = queryParams["scope"]?.split(" ".toRegex())?.dropLastWhile { it.isEmpty() }!!.toTypedArray()
        }

        // See if there is an error message, send the failure page
        val error = queryParams["error"]
        val errorDescription = queryParams["error_description"]

        //System.out.println("file: " + requestFilename);

        // Open the requested file.
       /* val fis: InputStream?
        val contentTypeLine: String
        if (requestFilename.startsWith("/auth.js") || requestFilename.startsWith("/auth-success.js")) {
            fis = javaClass.getResourceAsStream(requestFilename)
            contentTypeLine = "Content-type: text/javascript" + EOL
        } else {
            if (accessToken != null) {
                fis = successPage.openStream()
            } else if (error != null) {
                fis = failurePage.openStream()
            } else {
                fis = authPage.openStream()
            }
            contentTypeLine = "Content-type: text/html" + EOL
        }

        val fileExists = fis != null

        // Construct the response message.
        var statusLine: String? = null
        var entityBody: String? = null
        if (fileExists) {
            statusLine = "HTTP/1.1 200 OK" + EOL
        } else {
            statusLine = "HTTP/1.1 404 Not Found" + EOL
            entityBody = "404 Not Found"
        }

        // Send the status line.
        os.writeBytes(statusLine)

        // Send the content type line.
        os.writeBytes(contentTypeLine)

        // Send a blank line to indicate the end of the header lines.
        os.writeBytes(EOL)

        // Send the entity body.
        if (fileExists) {
            sendFileBytes(fis, os)
            fis!!.close()
        } else {
            os.writeBytes(entityBody!!)
        }
*/
        // Close streams and socket.
        os.close()
        br.close()
        socket.close()

        // Send callbacks
        if (authenticationListener != null) {
            // Send callback if access token received
            if (accessToken != null) {
                val accessScopes = arrayOfNulls<Scopes>(scopes.size)
                for (i in scopes.indices) {
                    accessScopes[i] = Scopes.fromString(scopes[i])
                }
                authenticationListener!!.onAccessTokenReceived(accessToken, *accessScopes as Array<out Scopes>)
            }
            // Send callback if authorization error
            if (error != null) {
                authenticationListener!!.onAuthenticationError(error, errorDescription!!)
            }
        }
    }

    companion object {

        private val EOL = "\r\n" // as specified by HTTP/1.1 spec

        /**
         * Send bytes from file input stream to the socket output stream.
         *
         * @param fis InputStream of the file contents.
         * @param os  OutputStream of the socket output stream.
         * @throws IOException if an I/O exception occurs.
         */
        @Throws(IOException::class)
        private fun sendFileBytes(fis: InputStream, os: OutputStream) {
            // Construct a 1K buffer to hold bytes on their way to the socket.
            val buffer = ByteArray(1024)
            var bytes = 0
            // Copy requested file into the socket's output stream.
            bytes = fis.read(buffer)
            while ((bytes) != -1) {
                os.write(buffer, 0, bytes)

                bytes = fis.read(buffer)
            }
        }

        /**
         * Extract the GET parameters from the HTTP request string.
         *
         * @param request HTTP request string
         * @return Map of all GET parameter key value pairs
         */
        private fun extractQueryParams(request: String): Map<String, String> {
            val params = HashMap<String, String>()

            val parts = request.split("\\?".toRegex(), 2).toTypedArray()
            if (parts.size < 2) {
                return params // No params
            }

            val query = parts[1]
            query.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    .asSequence()
                    .map { param -> param.split("=".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
                    .forEach {
                        try {
                            val key = URLDecoder.decode(it[0], "UTF-8")
                            var value = ""
                            if (it.size > 1) {
                                value = URLDecoder.decode(it[1], "UTF-8")
                            }
                            params.put(key, value)
                        } catch (ignored: UnsupportedEncodingException) {
                        }
                    }

            return params
        }
    }
}