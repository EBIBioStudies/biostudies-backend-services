package ebi.ac.uk.util.date

import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

fun OffsetDateTime.asIsoTime(): String = format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

fun fromIsoTime(timeString: String): OffsetDateTime = OffsetDateTime.parse(timeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

