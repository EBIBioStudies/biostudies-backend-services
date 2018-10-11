package ebi.ac.uk.funtions

import java.time.Clock
import java.time.Instant
import java.time.OffsetDateTime

/**
 * Calculate the equivalent date of the given epoch seconds.
 *
 * @param seconds the seconds to be transformed
 */
fun asIsoDate(seconds: Long): Instant = Instant.ofEpochSecond(seconds)


fun now(): OffsetDateTime = OffsetDateTime.now(Clock.systemUTC())