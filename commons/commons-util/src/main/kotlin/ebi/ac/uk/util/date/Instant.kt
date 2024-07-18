package ebi.ac.uk.util.date

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import java.time.temporal.ChronoUnit.DAYS

fun Instant.asOffsetAtStartOfDay(): OffsetDateTime = atOffset(UTC).toLocalDate().atStartOfDay().atOffset(UTC)

fun OffsetDateTime.atStartOfDay(): OffsetDateTime = toInstant().asOffsetAtStartOfDay()

fun Instant.asOffsetAtEndOfDay(): OffsetDateTime = plus(1, DAYS).atOffset(UTC).toLocalDate().atStartOfDay().minusSeconds(1).atOffset(UTC)
