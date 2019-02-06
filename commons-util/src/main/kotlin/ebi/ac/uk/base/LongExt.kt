package ebi.ac.uk.base

/**
 * Obtain the last n digit of the number decimal representation.
 */
fun Long.lastDigits(digits: Int): Long = this % (10.pow(digits))
