package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.common.validatedFilePath
import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.constants.FileFields
import org.w3c.dom.Node

class FileStandaloneXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer
) : BaseXmlDeserializer<BioFile>() {
    override fun deserialize(node: Node): BioFile {
        val path = node.getNodeAttribute(FileFields.PATH)

        return BioFile(
            path = validatedFilePath(path),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(FileFields.ATTRIBUTES))
        )
    }

    fun deserializeFilesTable(node: Node) = deserializeTable(node, FileFields.FILE.value, ::FilesTable)
}
