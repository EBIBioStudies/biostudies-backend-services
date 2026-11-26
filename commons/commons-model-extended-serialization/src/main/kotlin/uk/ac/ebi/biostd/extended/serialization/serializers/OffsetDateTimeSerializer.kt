package uk.ac.ebi.biostd.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import uk.ac.ebi.biostd.util.date.asIsoTime
import java.time.OffsetDateTime

class OffsetDateTimeSerializer : JsonSerializer<OffsetDateTime>() {
    override fun serialize(
        time: OffsetDateTime,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        gen.writeString(time.asIsoTime())
    }
}
