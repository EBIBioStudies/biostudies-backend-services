package uk.ac.ebi.extended.serialization.deserializers

import arrow.core.Either
import arrow.core.Either.Companion.right
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import uk.ac.ebi.extended.serialization.constants.ExtType.FilesTable
import uk.ac.ebi.extended.serialization.constants.ExtType.FireDirectory
import uk.ac.ebi.extended.serialization.constants.ExtType.FireFile
import uk.ac.ebi.extended.serialization.constants.ExtType.Link
import uk.ac.ebi.extended.serialization.constants.ExtType.LinksTable
import uk.ac.ebi.extended.serialization.constants.ExtType.NfsFile
import uk.ac.ebi.extended.serialization.constants.ExtType.Section
import uk.ac.ebi.extended.serialization.constants.ExtType.SectionsTable
import uk.ac.ebi.serialization.extensions.convertNode
import uk.ac.ebi.serialization.extensions.getNode

class EitherExtTypeDeserializer : JsonDeserializer<Either<*, *>>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): Either<*, *> {
        val mapper = jsonParser.codec as ObjectMapper
        val node = mapper.readTree<JsonNode>(jsonParser)
        val extType = node.getNode<TextNode>(EXT_TYPE).textValue()

        // TODO here is where a general system property would come in handy since we could base the deserializing
        // TODO decision in that parameter instead of requiring to be explicitly in the model, that way, it would keep
        // TODO transparent for whoever is consuming the extended model
        return when (ExtType.valueOf(extType)) {
            is Link -> Either.left(mapper.convertNode<ExtLink>(node))
            is NfsFile -> Either.left(mapper.convertNode<ExtFile>(node))
            is FireFile -> TODO()
            is FireDirectory -> TODO()
            is Section -> Either.left(mapper.convertNode<ExtSection>(node))
            is LinksTable -> right(mapper.convertNode<ExtLinkTable>(node))
            is FilesTable -> right(mapper.convertNode<ExtFileTable>(node))
            is SectionsTable -> right(mapper.convertNode<ExtSectionTable>(node))
        }
    }
}
