package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlCollection
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlField
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlObj
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.LinkFields
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class LinkSerializer : XmlStdSerializer<Link>(Link::class.java) {

    override fun serializeXml(value: Link, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(LinkFields.LINK, value) {
                writeXmlField(LinkFields.URL, value.url)
                writeXmlCollection(LinkFields.ATTRIBUTES, value.attributes)
            }
        }
    }
}
