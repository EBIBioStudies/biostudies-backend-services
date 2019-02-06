package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.SectionFields
import org.w3c.dom.Node

class SectionXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer,
    private val linkXmlDeserializer: LinkXmlDeserializer,
    private val fileXmlDeserializer: FileXmlDeserializer
) : BaseXmlDeserializer<Section>() {

    override fun deserialize(node: Node): Section {
        return Section(
            accNo = node.findProperty(SectionFields.ACC_NO),
            type = node.getProperty(SectionFields.TYPE),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(SectionFields.ATTRIBUTES)),
            links = linkXmlDeserializer.deserializeTableList(node.findNode(SectionFields.LINKS), LinkFields.LINK.value, ::LinksTable),
            files = fileXmlDeserializer.deserializeTableList(node.findNode(SectionFields.FILES), FileFields.FILE.value, ::FilesTable),
            sections = deserializeTableList(node.findNode(SectionFields.SUBSECTIONS), SectionFields.SECTION.value, ::SectionsTable))
    }
}
