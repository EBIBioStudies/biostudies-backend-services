package ebi.ac.uk.base

import java.time.Instant

fun asIsoDate(seconds: Long): Instant = Instant.ofEpochSecond(seconds)