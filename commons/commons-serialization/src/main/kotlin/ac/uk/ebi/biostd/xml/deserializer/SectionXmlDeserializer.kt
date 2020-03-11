package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.SectionFields.ACC_NO
import ebi.ac.uk.model.constants.SectionFields.ATTRIBUTES
import ebi.ac.uk.model.constants.SectionFields.FILES
import ebi.ac.uk.model.constants.SectionFields.LINKS
import ebi.ac.uk.model.constants.SectionFields.SECTION
import ebi.ac.uk.model.constants.SectionFields.SUBSECTIONS
import ebi.ac.uk.model.constants.SectionFields.TYPE
import org.w3c.dom.Node

class SectionXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer,
    private val linkXmlDeserializer: LinkXmlDeserializer,
    private val fileXmlDeserializer: FileXmlDeserializer
) : BaseXmlDeserializer<Section>() {
    override fun deserialize(node: Node): Section =
        Section(
            accNo = node.findProperty(ACC_NO),
            type = node.getProperty(TYPE),
            attributes = attributeXmlDeserializer.deserializeList(node.findNode(ATTRIBUTES)),
            links = linkXmlDeserializer.deserializeTableList(node.findNode(LINKS), LinkFields.LINK.value, ::LinksTable),
            files = fileXmlDeserializer.deserializeTableList(node.findNode(FILES), FileFields.FILE.value, ::FilesTable))
            .apply { addSections(deserializeTableList(node.findNode(SUBSECTIONS), SECTION.value, ::SectionsTable)) }
}
