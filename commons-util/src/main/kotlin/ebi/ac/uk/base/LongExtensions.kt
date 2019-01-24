package ebi.ac.uk.base

import kotlin.math.pow

/**
 * Calculate pow to the given exponent.
 */
fun Int.pow(pow: Int) = this.toDouble().pow(pow).toLong()

/**
 * Obtain the last n digit of the number decimal representation.
 */
fun Long.lastDigits(digits: Int): Long = this % (10.pow(digits))
