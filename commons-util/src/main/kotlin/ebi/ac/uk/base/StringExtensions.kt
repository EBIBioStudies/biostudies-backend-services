package ebi.ac.uk.base

import arrow.core.Option

const val EMPTY = ""

/**
 * Return true if the given String is NOT empty nor null other.
 */
fun String?.isNotBlank() = !isNullOrEmpty()

/**
 * Execute the provided lambda is empty is not empty or null.
 */
inline fun String?.applyIfNotBlank(func: (String) -> Unit) = takeIf { it.isNotBlank() }?.let { func(it) }

fun String?.toOption() = if (isNullOrEmpty()) Option.empty() else Option.fromNullable(this)
