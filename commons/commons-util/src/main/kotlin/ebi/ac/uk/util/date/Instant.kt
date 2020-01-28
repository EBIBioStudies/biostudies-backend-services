package ebi.ac.uk.util.date

import java.time.Instant
import java.util.Calendar
import java.util.TimeZone

@Suppress("LongParameterList")
fun createInstant(year: Int, month: Int, day: Int, hours: Int, minutes: Int, seconds: Int): Instant {
    return Calendar.Builder()
        .setDate(year, month, day)
        .setTimeOfDay(hours, minutes, seconds)
        .setTimeZone(TimeZone.getTimeZone("UTC"))
        .build()
        .toInstant()
}
