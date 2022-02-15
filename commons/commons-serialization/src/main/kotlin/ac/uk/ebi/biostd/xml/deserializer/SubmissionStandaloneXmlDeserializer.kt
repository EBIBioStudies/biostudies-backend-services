package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields
import org.w3c.dom.Node

class SubmissionStandaloneXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer,
    private val sectionXmlDeserializer: SectionStandaloneXmlDeserializer
) : BaseXmlDeserializer<Submission>() {
    override fun deserialize(node: Node): Submission = Submission(
        accNo = node.getProperty(SubFields.ACC_NO),
        attributes = attributeXmlDeserializer.deserializeList(node.findNode(SubFields.ATTRIBUTES)),
        section = sectionXmlDeserializer.deserialize(node.getNode(SubFields.SECTION))
    )
}
