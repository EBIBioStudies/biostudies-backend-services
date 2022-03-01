package ebi.ac.uk.util.regex

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Tries to match the pattern over the provided string and returns an optional containing the matcher result.
 */
fun Pattern.match(expression: String): Matcher? {
    val matcher = matcher(expression)
    return if (matcher.matches()) matcher else null
}
