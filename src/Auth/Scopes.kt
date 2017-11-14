package Auth

/**
 * Created by beepbeat/holladiewal on 13.11.2017.
 */

/**
 * When requesting authorization from users, the scope parameter allows you to specify
 * which permissions your app requires. These scopes are ties to the access token you
 * receive upon a successful authorization. Without specifying scopes, your app only has
 * access to basic information about the authenticated user. You may specify any or all
 * of the following scopes.
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
enum class Scopes private constructor(
        /**
         * Get the identifier that twitch will recognize.
         *
         * @return A `String` identifier
         */
        val key: String) {

    /**
     * Read access to non-public user information, such as email address.
     */
    USER_READ("user_read"),

    /**
     * Ability to ignore or unignore on behalf of a user.
     */
    USER_BLOCKS_EDIT("user_blocks_edit"),

    /**
     * Read access to a user's list of ignored users.
     */
    USER_BLOCKS_READ("user_blocks_read"),

    /**
     * Access to manage a user's followed channels.
     */
    USER_FOLLOWS_EDIT("user_follows_edit"),

    /**
     * Read access to non-public channel information, including email address and stream key.
     */
    CHANNEL_READ("channel_read"),

    /**
     * Write access to channel metadata (game, status, etc).
     */
    CHANNEL_EDITOR("channel_editor"),

    /**
     * Access to trigger commercials on channel.
     */
    CHANNEL_COMMERCIAL("channel_commercial"),

    /**
     * Ability to reset a channel's stream key.
     */
    CHANNEL_STREAM("channel_stream"),

    /**
     * Read access to all subscribers to your channel.
     */
    CHANNEL_SUBSCRIPTIONS("channel_subscriptions"),

    /**
     * Read access to subscriptions of a user.
     */
    USER_SUBSCRIPTIONS("user_subscriptions"),

    /**
     * Read access to check if a user is subscribed to your channel.
     */
    CHANNEL_CHECK_SUBSCRIPTION("channel_check_subscription"),

    /**
     * Ability to log into chat and send messages.
     */
    CHAT_LOGIN("chat_login");

    override fun toString(): String {
        return key
    }

    companion object {

        /**
         * Combine `Auth.Scopes` into a '+' separated `String`.
         * This is the required input format for twitch.tv
         *
         * @param scopes `Auth.Scopes` to combine.
         * @return `String` representing '+' separated list of `Auth.Scopes`
         */
        fun join(vararg scopes: Scopes): String {
            if (scopes == null) return ""
            val sb = StringBuilder()
            for (scope in scopes) {
                sb.append(scope.key).append("+")
            }
            return sb.toString()
        }

        /**
         * Convert the string representation of the Scope to the Enum.
         *
         * @param text Text representation of Enum value
         * @return Enum value that the text represents
         */
        fun fromString(text: String?): Scopes? {
            if (text == null) return null
            for (b in Scopes.values()) {
                if (text.equals(b.key, ignoreCase = true)) {
                    return b
                }
            }
            return null
        }
    }
}
