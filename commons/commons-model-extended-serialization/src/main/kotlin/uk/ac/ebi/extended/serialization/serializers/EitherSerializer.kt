package uk.ac.ebi.extended.serialization.serializers

import arrow.core.Either
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class EitherSerializer : JsonSerializer<Either<*, *>>() {
    override fun serialize(either: Either<*, *>, gen: JsonGenerator, serializers: SerializerProvider) {
        either.fold({ gen.writeObject(it) }, { gen.writeObject(it) })
    }
}
