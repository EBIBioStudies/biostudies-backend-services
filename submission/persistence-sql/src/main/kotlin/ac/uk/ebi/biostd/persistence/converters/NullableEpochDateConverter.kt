package ac.uk.ebi.biostd.persistence.converters

import ebi.ac.uk.functions.secondsToInstant
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import javax.persistence.AttributeConverter

internal class NullableEpochDateConverter : AttributeConverter<OffsetDateTime?, Long> {
    override fun convertToEntityAttribute(seconds: Long): OffsetDateTime? =
        if (seconds == -1L) null else secondsToInstant(seconds).atOffset(UTC)

    override fun convertToDatabaseColumn(dbData: OffsetDateTime?): Long = dbData?.toEpochSecond() ?: -1
}

class EpochDateConverter : AttributeConverter<OffsetDateTime, Long> {
    override fun convertToEntityAttribute(seconds: Long): OffsetDateTime = secondsToInstant(seconds).atOffset(UTC)

    override fun convertToDatabaseColumn(dbData: OffsetDateTime): Long = dbData.toEpochSecond()
}
