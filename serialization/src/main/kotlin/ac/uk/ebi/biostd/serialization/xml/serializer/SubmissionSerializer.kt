package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlAttr
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlCollection
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlField
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlObj
import ac.uk.ebi.biostd.submission.SubFields
import ac.uk.ebi.biostd.submission.Submission
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class SubmissionSerializer : XmlStdSerializer<Submission>(Submission::class.java) {

    override fun serializeXml(value: Submission, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(SubFields.SUBMISSION, value) {
                writeXmlAttr(SubFields.ACC_NO, accNo)
                writeXmlCollection(SubFields.ATTRIBUTES, value.allAttributes())
                writeXmlField(SubFields.SECTION, section)
            }
        }
    }
}
