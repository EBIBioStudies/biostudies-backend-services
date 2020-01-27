package ebi.ac.uk.util.date

import java.time.OffsetDateTime
import java.time.OffsetDateTime.parse
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun OffsetDateTime.asIsoTime(): String = format(ISO_OFFSET_DATE_TIME)

fun OffsetDateTime.isBeforeOrEqual(other: OffsetDateTime): Boolean = isBefore(other) || isEqual(other)

fun fromIsoTime(timeString: String): OffsetDateTime = parse(timeString, ISO_OFFSET_DATE_TIME)

fun max(one: OffsetDateTime, another: OffsetDateTime): OffsetDateTime = if (one.isAfter(another)) one else another
