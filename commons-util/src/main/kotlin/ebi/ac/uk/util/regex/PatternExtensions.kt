package ebi.ac.uk.util.regex

import arrow.core.Option
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Try to match the pattern over the provided string and return optional of matcher.
 */
fun Pattern.match(expression: String): Option<Matcher> {
    val matcher = matcher(expression)
    return when {
        matcher.matches() -> Option.just(matcher)
        else -> Option.empty()
    }
}
