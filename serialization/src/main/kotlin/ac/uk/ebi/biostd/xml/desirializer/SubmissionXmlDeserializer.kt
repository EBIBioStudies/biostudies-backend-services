package ac.uk.ebi.biostd.xml.desirializer

import ac.uk.ebi.biostd.xml.desirializer.common.BaseXmlDeserializer
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields
import org.w3c.dom.Node

class SubmissionXmlDeserializer(
    private val attributeXmlDeserializer: AttributeXmlDeserializer,
    private val sectionXmlDeserializer: SectionXmlDeserializer
)
    : BaseXmlDeserializer<Submission>() {

    override fun deserialize(node: Node): Submission {
        return Submission(
            accNo = node.getProperty(SubFields.ACC_NO),
            attributes = attributeXmlDeserializer.deserializeList(node.getNode(SubFields.ATTRIBUTES)),
            section = sectionXmlDeserializer.deserialize(node.getNode(SubFields.SECTION))
        )
    }
}
