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
/**
 * The `Auth.AuthenticationError` class represents an error during
 * authentication with [http://www.twitch.tv](http://twitch.tv).
 */
class AuthenticationError(
        /**
         * Get the name of the error.
         *
         * @return the name of the error
         */
        val name: String? // the name of the error
        ,
        /**
         * Get the description of the error.
         *
         * @return the description of the error.
         */
        val description: String? // the description of the error
) {

    override fun toString(): String {
        return "Auth.AuthenticationError{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}'
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as AuthenticationError?

        return if (if (name != null) name != that!!.name else that!!.name != null) false else !if (description != null) description != that.description else that.description != null

    }

    override fun hashCode(): Int {
        var result = name?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        return result
    }
}
