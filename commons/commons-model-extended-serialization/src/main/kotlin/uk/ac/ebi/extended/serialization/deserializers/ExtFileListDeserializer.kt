package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import org.springframework.web.client.getForObject
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.service.ExtSerializationService.Companion.mapper
import uk.ac.ebi.extended.serialization.service.ExtSerializationService.Companion.restTemplate
import uk.ac.ebi.serialization.extensions.getNode

class ExtFileListDeserializer : JsonDeserializer<ExtFileList>() {
    override fun deserialize(jsonParser: JsonParser, ctxt: DeserializationContext): ExtFileList {
        val mapper = jsonParser.codec as ObjectMapper
        val node: JsonNode = mapper.readTree(jsonParser)

        return ExtFileList(
            fileName = node.getNode<TextNode>(FILE_NAME).textValue(),
            files = loadFiles(node.getNode<TextNode>(FILES).textValue())
        )
    }

    private fun loadFiles(url: String): List<ExtFile> {
        val referencedFiles = restTemplate.getForObject<String>(url)
        return mapper.readValue(referencedFiles, ExtFileTable::class.java).files
    }
}
