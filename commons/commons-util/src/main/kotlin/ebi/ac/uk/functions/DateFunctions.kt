package ebi.ac.uk.functions

import java.time.Instant

/**
 * Calculate the equivalent date of the given epoch seconds.
 *
 * @param seconds the seconds to be transformed
 */
fun secondsToInstant(seconds: Long): Instant = Instant.ofEpochSecond(seconds)

/**
 * Calculate the equivalent date of the given epoch seconds.
 *
 * @param seconds the seconds to be transformed
 */
fun milisToInstant(seconds: Long): Instant = Instant.ofEpochMilli(seconds)
