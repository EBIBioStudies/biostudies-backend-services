package ebi.ac.uk.base

const val EMPTY = ""

inline fun String?.applyIfNotNullOrEmpty(func: (String) -> Unit) = this.takeIf { !it.isNullOrEmpty() }?.let { func(it) }
