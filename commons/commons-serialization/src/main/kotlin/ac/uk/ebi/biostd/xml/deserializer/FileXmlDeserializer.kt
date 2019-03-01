package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.constants.FileFields
import org.w3c.dom.Node

class FileXmlDeserializer(private val attributeXmlDeserializer: AttributeXmlDeserializer)
    : BaseXmlDeserializer<File>() {

    override fun deserialize(node: Node): File {
        return File(
            path = node.getNodeAttribute(FileFields.PATH),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(FileFields.ATTRIBUTES))
        )
    }

    fun deserializeFilesTable(node: Node) = deserializeTable(node, FileFields.FILE.value, ::FilesTable)
}
