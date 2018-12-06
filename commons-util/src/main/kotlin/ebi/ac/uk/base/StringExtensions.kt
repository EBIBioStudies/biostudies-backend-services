package ebi.ac.uk.base

import arrow.core.Option
import org.apache.commons.lang3.BooleanUtils

const val EMPTY = ""

/**
 * Return true if the given String is NOT empty nor null other.
 */
fun String?.isNotBlank() = !isNullOrEmpty()

/**
 * Execute the provided lambda is empty is not empty or null.
 */
inline fun String?.applyIfNotBlank(func: (String) -> Unit) = takeIf { it.isNotBlank() }?.let { func(it) }

/**
 * Transforms the nullable string into optional, Option.empty() if string is empty or null.
 */
fun String?.toOption() = if (isNullOrEmpty()) Option.empty() else Option.fromNullable(this)

/**
 * Transforms the string into boolean.
 *
 *   (null).asBoolean    = false
 *   ("true").asBoolean  = true
 *   ("TRUE").asBoolean  = true
 *   ("tRUe").asBoolean  = true
 *   ("on").asBoolean    = true
 *   ("yes").asBoolean   = true
 *   ("false").asBoolean = false
 *   ("x gti").asBoolean = false
 */
fun String.asBoolean() = BooleanUtils.toBoolean(this)

/**
 * Compare if string representation of objects are equivalent, ignoring case.
 */
infix fun String.like(other: Any) = other.toString().equals(this, ignoreCase = true)