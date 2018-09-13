package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeAttr
import ac.uk.ebi.biostd.serialization.xml.extensions.writeField
import ac.uk.ebi.biostd.serialization.xml.extensions.writeObj
import ac.uk.ebi.biostd.submission.Submission
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class SubmissionSerializer : XmlStdSerializer<Submission>(Submission::class.java) {

    override fun serializeXml(value: Submission, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeObj("submission", value) {
                writeAttr("acc", accNo)
                writeField("attributes", attributes)
            }
        }
    }
}
