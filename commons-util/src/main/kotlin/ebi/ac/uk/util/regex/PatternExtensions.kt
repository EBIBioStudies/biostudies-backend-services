package ebi.ac.uk.util.regex

import arrow.core.Option
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Tries to match the pattern over the provided string and returns an optional containing the matcher result.
 */
fun Pattern.match(expression: String): Option<Matcher> {
    val matcher = matcher(expression)
    return when {
        matcher.matches() -> Option.just(matcher)
        else -> Option.empty()
    }
}
