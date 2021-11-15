package uk.ac.ebi.serialization.deserializers

import arrow.core.Either
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import uk.ac.ebi.serialization.extensions.tryConvertValue

/**
 * Either deserializer, try to unserialize each either wrapper type to obtain the correct representation.
 */
class EitherDeserializer : StdDeserializer<Either<*, *>>(Either::class.java), ContextualDeserializer {
    private lateinit var leftType: JavaType
    private lateinit var rightType: JavaType

    override fun createContextual(ctxt: DeserializationContext, property: BeanProperty?): JsonDeserializer<*> {
        val wrapperType = ctxt.contextualType

        return EitherDeserializer().apply {
            leftType = wrapperType.containedType(0)
            rightType = wrapperType.containedType(1)
        }
    }

    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): Either<*, *> {
        with(jp.codec as ObjectMapper) {
            val node: JsonNode = readTree(jp)
            return tryConvertValue(node, rightType)?.let { Either.Right(it) }
                ?: tryConvertValue(node, leftType)?.let { Either.Left(it) }
                ?: throw IllegalStateException("can not deserialize $node into $leftType neither $rightType")
        }
    }
}
