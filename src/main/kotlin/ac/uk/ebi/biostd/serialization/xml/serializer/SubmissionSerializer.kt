package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.common.writeAttr
import ac.uk.ebi.biostd.submission.Submission
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class SubmissionSerializer : XmlStdSerializer<Submission>(Submission::class.java) {

    override fun serializeXml(value: Submission, gen: ToXmlGenerator, provider: SerializerProvider) {
        gen.writeStartObject()
        gen.writeAttr("acc", value.accNo)
        gen.writeObjectField("attributes", value.attributes)
        gen.writeObjectField("section", value.section)
        gen.writeEndObject()
    }
}
