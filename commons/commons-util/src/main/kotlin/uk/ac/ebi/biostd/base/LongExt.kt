package uk.ac.ebi.biostd.base

/**
 * Obtain the last n digit of the number decimal representation.
 */
@Suppress("MagicNumber")
fun Long.lastDigits(digits: Int): Long = this % (10.pow(digits))
