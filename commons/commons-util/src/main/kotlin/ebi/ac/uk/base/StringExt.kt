@file:Suppress("TooManyFunctions")

package ebi.ac.uk.base

const val EMPTY = ""

/**
 * Transform the given string into null if it is nullable.
 */
fun String?.nullIfBlank(): String? =
    when {
        this == null -> null
        isBlank() -> null
        else -> this
    }

/**
 * Return true if the given String is NOT empty nor null other.
 */
fun String?.isNotBlank() = !isNullOrEmpty()

/**
 * Execute the provided lambda is empty is not empty or null.
 */
inline fun String?.applyIfNotBlank(func: (String) -> Unit) = takeIf { it.isNotBlank() }?.let { func(it) }

/**
 * Compare if string representations of the given objects are equivalent, ignoring its case.
 */
infix fun String.like(other: Any) = other.toString().equals(this, ignoreCase = true)

/**
 * Split the string by the given regex, ignoring empty results.
 */
fun String.splitIgnoringEmpty(regex: Regex) = this.split(regex).filter { it.isNotBlank() }

/**
 * Removes the regex expression matching the string.
 */
fun String.remove(regex: Regex) = replace(regex, "")

/**
 * Removes the first regex expression matching the string.
 */
fun String.removeFirstOccurrence(regex: Regex) = replaceFirst(regex, "")

/**
 * Escape the given @see toScape string by using provided scape literal @see scapeLiteral or backslash if non is
 * provided.
 */
fun String.scape(
    toScape: String,
    scapeLiteral: String = "\\",
): String = replace(toScape, "$scapeLiteral$toScape")

/**
 * Replaces multiple whitespaces with a single space.
 */
fun String.trim(): String = replace("\\s+".toRegex(), " ")

/**
 * Compute the given string, so it always ends in theprovided suffix. No action will be taken if the string already
 * finishes with the expected suffix.
 */
fun String.ensureSuffix(suffix: String): String = if (endsWith(suffix)) this else "$this$suffix"
