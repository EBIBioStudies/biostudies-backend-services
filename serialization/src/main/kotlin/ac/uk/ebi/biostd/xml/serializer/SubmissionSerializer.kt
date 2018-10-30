package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlCollection
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.SubFields
import ebi.ac.uk.model.Submission

class SubmissionSerializer : XmlStdSerializer<Submission>(Submission::class.java) {

    override fun serializeXml(value: Submission, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(SubFields.SUBMISSION, value) {
                writeXmlCollection(SubFields.ATTRIBUTES, value.attributes)
                writeXmlField(SubFields.SECTION, rootSection)
            }
        }
    }
}
