package ebi.ac.uk.base

import kotlin.math.pow

/**
 * Calculate pow to the given exponent.
 */
fun Int.pow(pow: Int) = this.toDouble().pow(pow).toLong()
