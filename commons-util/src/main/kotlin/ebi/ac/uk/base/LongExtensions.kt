package ebi.ac.uk.base

import kotlin.math.pow


fun Int.pow(pow: Int) = this.toDouble().pow(pow).toLong()

fun Long.lastDigits(digits: Int): Long = this % (10.pow(digits))