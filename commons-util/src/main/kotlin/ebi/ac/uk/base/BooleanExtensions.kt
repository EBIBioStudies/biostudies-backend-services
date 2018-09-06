package ebi.ac.uk.base

/**
 * Return nullable boolean value or false if it is null.
 *
 */
fun Boolean?.orFalse(): Boolean = this ?: false
