package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.constants.FileFields
import org.w3c.dom.Node

class FileStandaloneXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer
) : BaseXmlDeserializer<File>() {
    override fun deserialize(node: Node): File {
        val path = node.getNodeAttribute(FileFields.PATH)
        require(path.isNotBlank()) { throw InvalidElementException(REQUIRED_FILE_PATH) }

        return File(
            path = path,
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(FileFields.ATTRIBUTES))
        )
    }

    fun deserializeFilesTable(node: Node) = deserializeTable(node, FileFields.FILE.value, ::FilesTable)
}
