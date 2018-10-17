package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.LinkFields
import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlCollection
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
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
