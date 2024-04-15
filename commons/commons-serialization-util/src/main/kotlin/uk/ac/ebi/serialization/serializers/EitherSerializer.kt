package uk.ac.ebi.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.base.Either

/**
 * Either Serializer, fold and serialize either wrapped value.
 */
class EitherSerializer : StdSerializer<Either<*, *>>(Either::class.java) {
    override fun serialize(
        either: Either<*, *>,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        either.fold({ gen.writeObject(it) }, { gen.writeObject(it) })
    }
}
