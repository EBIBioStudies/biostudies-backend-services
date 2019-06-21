package ebi.ac.uk.util.date

import java.time.OffsetDateTime
import java.time.OffsetDateTime.parse
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun OffsetDateTime.asIsoTime(): String = format(ISO_OFFSET_DATE_TIME)

fun fromIsoTime(timeString: String): OffsetDateTime = parse(timeString, ISO_OFFSET_DATE_TIME)
