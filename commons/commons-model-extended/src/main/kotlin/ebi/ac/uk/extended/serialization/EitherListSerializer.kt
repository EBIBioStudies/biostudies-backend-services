package ebi.ac.uk.extended.serialization

import ac.uk.ebi.biostd.common.EitherSerializer
import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class EitherListSerializer : JsonSerializer<List<Either<*, *>>>() {
    override fun serialize(value: List<Either<*, *>>, gen: JsonGenerator, serializers: SerializerProvider) {
        val eitherSerializer = EitherSerializer()

        gen.writeStartArray()
        value.forEach { eitherSerializer.serialize(it, gen, serializers) }
        gen.writeEndArray()
    }
}
