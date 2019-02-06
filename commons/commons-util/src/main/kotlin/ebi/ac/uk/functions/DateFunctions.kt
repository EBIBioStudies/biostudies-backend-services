package ebi.ac.uk.functions

import java.time.Instant

/**
 * Calculate the equivalent date of the given epoch seconds.
 *
 * @param seconds the seconds to be transformed
 */
fun asInstant(seconds: Long): Instant = Instant.ofEpochSecond(seconds)
