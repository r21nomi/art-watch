package com.nomi.artwatch.util

import java.util.*

/**
 *
 * `StringUtils` instances should NOT be constructed in
 * standard programming. Instead, the class should be used as
 * `StringUtils.trim(" foo ");`.
 *
 *
 *
 * This constructor is public to permit tools that require a JavaBean
 * instance to operate.
 */
class StringUtil {
    companion object {

        /**
         * The empty String `""`.

         * @since 2.0
         */
        val EMPTY = ""

        // Left/Right/Mid
        //-----------------------------------------------------------------------

        /**
         *
         * Gets the leftmost `len` characters of a String.
         *
         *
         *
         * If `len` characters are not available, or the
         * String is `null`, the String will be returned without
         * an exception. An empty String is returned if len is negative.
         *
         *
         * <pre>
         * StringUtils.left(null, *)    = null
         * StringUtils.left(*, -ve)     = ""
         * StringUtils.left("", *)      = ""
         * StringUtils.left("abc", 0)   = ""
         * StringUtils.left("abc", 2)   = "ab"
         * StringUtils.left("abc", 4)   = "abc"
        </pre> *

         * @param str the String to get the leftmost characters from, may be null
         * *
         * @param len the length of the required String
         * *
         * @return the leftmost characters, `null` if null String input
         */
        fun left(str: String?, len: Int): String? {
            if (str == null) {
                return null
            }
            if (len < 0) {
                return EMPTY
            }
            if (str.length <= len) {
                return str
            }
            return str.substring(0, len)
        }

        /**
         *
         * Gets the rightmost `len` characters of a String.
         *
         *
         *
         * If `len` characters are not available, or the String
         * is `null`, the String will be returned without an
         * an exception. An empty String is returned if len is negative.
         *
         *
         * <pre>
         * StringUtils.right(null, *)    = null
         * StringUtils.right(*, -ve)     = ""
         * StringUtils.right("", *)      = ""
         * StringUtils.right("abc", 0)   = ""
         * StringUtils.right("abc", 2)   = "bc"
         * StringUtils.right("abc", 4)   = "abc"
        </pre> *

         * @param str the String to get the rightmost characters from, may be null
         * *
         * @param len the length of the required String
         * *
         * @return the rightmost characters, `null` if null String input
         */
        fun right(str: String?, len: Int): String? {
            if (str == null) {
                return null
            }
            if (len < 0) {
                return EMPTY
            }
            if (str.length <= len) {
                return str
            }
            return str.substring(str.length - len)
        }

        /**
         *
         * Splits the provided text into an array, separator string specified.
         *
         *
         *
         * The separator(s) will not be included in the returned String array.
         * Adjacent separators are treated as one separator.
         *
         *
         *
         * A `null` input String returns `null`.
         * A `null` separator splits on whitespace.
         *
         *
         * <pre>
         * StringUtils.splitByWholeSeparator(null, *)               = null
         * StringUtils.splitByWholeSeparator("", *)                 = []
         * StringUtils.splitByWholeSeparator("ab de fg", null)      = ["ab", "de", "fg"]
         * StringUtils.splitByWholeSeparator("ab   de fg", null)    = ["ab", "de", "fg"]
         * StringUtils.splitByWholeSeparator("ab:cd:ef", ":")       = ["ab", "cd", "ef"]
         * StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-") = ["ab", "cd", "ef"]
        </pre> *

         * @param str           the String to parse, may be null
         * *
         * @param separatorChar char containing the String to be used as a delimiter,
         * *                      `null` splits on whitespace
         * *
         * @return a list of parsed Strings, `null` if null String was input
         */
        fun split(str: String, separatorChar: Char): List<String>? {
            return splitWorker(str, separatorChar, true)
        }

        /**
         * Performs the logic for the `split` and
         * `splitPreserveAllTokens` methods that do not return a
         * maximum array length.

         * @param str               the String to parse, may be `null`
         * *
         * @param separatorChar     the separate character
         * *
         * @param preserveAllTokens if `true`, adjacent separators are
         * *                          treated as empty token separators; if `false`, adjacent
         * *                          separators are treated as one separator.
         * *
         * @return an list of parsed Strings, `null` if null String input
         */
        private fun splitWorker(str: String?, separatorChar: Char, preserveAllTokens: Boolean): List<String>? {
            // Performance tuned for 2.0 (JDK1.4)

            if (str == null) {
                return null
            }
            val len = str.length
            if (len == 0) {
                return ArrayList()
            }
            val list = ArrayList<String>()
            var i = 0
            var start = 0
            var match = false
            var lastMatch = false
            while (i < len) {
                if (str[i] == separatorChar) {
                    if (match || preserveAllTokens) {
                        list.add(str.substring(start, i))
                        match = false
                        lastMatch = true
                    }
                    start = ++i
                    continue
                }
                lastMatch = false
                match = true
                i++
            }
            if (match || preserveAllTokens && lastMatch) {
                list.add(str.substring(start, i))
            }
            return list
        }
    }
}
