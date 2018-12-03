package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.xml.desirializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constans.FileFields
import ebi.ac.uk.model.constans.LinkFields
import ebi.ac.uk.model.constans.SectionFields
import org.w3c.dom.Node

class SectionXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer,
    private val linkXmlDeserializer: LinkXmlDeserializer,
    private val fileXmlDeserializer: FileXmlDeserializer
)
    : BaseXmlDeserializer<Section>() {

    override fun deserialize(node: Node): Section {
        return Section(
            accNo = node.findProperty(SectionFields.ACC_NO),
            type = node.getProperty(SectionFields.TYPE),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(SectionFields.ATTRIBUTES)),
            links = linkXmlDeserializer.deserializeTableList(node.findNode(SectionFields.LINKS), LinkFields.LINK.value, ::LinksTable),
            files = fileXmlDeserializer.deserializeTableList(node.findNode(SectionFields.FILES), FileFields.FILE.value, ::FilesTable),
            sections = deserializeTableList(node.findNode(SectionFields.SUBSECTIONS), SectionFields.SUBSECTIONS.value, ::SectionsTable))
    }
}