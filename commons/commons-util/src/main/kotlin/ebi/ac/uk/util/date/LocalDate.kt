package ebi.ac.uk.util.date

import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

fun LocalDate.asOffsetAtStartOfDay(): OffsetDateTime = atStartOfDay().atOffset(UTC)

fun LocalDate.asOffsetAtEndOfDay(): OffsetDateTime = plusDays(1).atStartOfDay().minusSeconds(1).atOffset(UTC)
