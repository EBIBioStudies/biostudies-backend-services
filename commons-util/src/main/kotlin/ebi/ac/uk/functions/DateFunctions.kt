package ebi.ac.uk.functions

import java.time.Instant

/**
 * Calculate the equivalent date of the given epoch seconds.
 *
 * @param seconds the seconds to be transformed
 */
fun asIsoDate(seconds: Long): Instant = Instant.ofEpochSecond(seconds)


fun now(): Instant = Instant.now()