package uk.ac.ebi.biostd.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.ac.ebi.biostd.base.Either
import uk.ac.ebi.biostd.base.Either.Left
import uk.ac.ebi.biostd.base.Either.Right

/**
 * Either deserializer, try to deserialize each either wrapper type to get the correct representation.
 */
class EitherDeserializer :
    StdDeserializer<Either<*, *>>(Either::class.java),
    ContextualDeserializer {
    private lateinit var leftType: JavaType
    private lateinit var rightType: JavaType

    override fun createContextual(
        ctxt: DeserializationContext,
        property: BeanProperty?,
    ): JsonDeserializer<*> {
        val wrapperType = ctxt.contextualType

        return EitherDeserializer().apply {
            leftType = wrapperType.containedType(0)
            rightType = wrapperType.containedType(1)
        }
    }

    override fun deserialize(
        jp: JsonParser,
        ctxt: DeserializationContext,
    ): Either<*, *> {
        with(jp.codec as ObjectMapper) {
            val node: JsonNode = readTree(jp)

            return when (node.isArray) {
                true -> Right(convertValue<Any>(node, rightType))
                else -> Left(convertValue<Any>(node, leftType))
            }
        }
    }
}
