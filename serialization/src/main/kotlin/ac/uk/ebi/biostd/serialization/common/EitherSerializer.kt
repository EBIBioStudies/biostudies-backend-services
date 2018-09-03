package ac.uk.ebi.biostd.serialization.common

import ac.uk.ebi.biostd.extensions.tryConvertValue
import arrow.core.*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class EitherSerializer : StdSerializer<Either<*, *>>(Either::class.java) {
    override fun serialize(either: Either<*, *>?, gen: JsonGenerator?, provider: SerializerProvider?) {
        either?.fold({ gen?.writeObject(it) }, { gen?.writeObject(it) })
    }
}

class EitherDeserializer : StdDeserializer<Either<*, *>>(Either::class.java), ContextualDeserializer {
    private lateinit var leftType: JavaType
    private lateinit var rightType: JavaType

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty): JsonDeserializer<*> {
        val wrapperType = property.type.containedType(0)
        val deserializer = EitherDeserializer()
        deserializer.leftType = wrapperType.containedType(0)
        deserializer.rightType = wrapperType.containedType(1)
        return deserializer
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Either<*, *> {
        val mapper = jp.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jp)

        return mapper.tryConvertValue(node, leftType).map { Either.Left(it) }
                .or(mapper.tryConvertValue(node, rightType).map { Either.Right(it) })
                .getOrElse { throw IllegalStateException("can not deserialize $node into $leftType neither $rightType") }
    }
}
