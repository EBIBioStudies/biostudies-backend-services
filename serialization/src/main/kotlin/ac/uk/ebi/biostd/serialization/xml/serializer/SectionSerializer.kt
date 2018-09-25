package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlAttr
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlCollection
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlObj
import ac.uk.ebi.biostd.submission.Section
import ac.uk.ebi.biostd.submission.SectionFields
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class SectionSerializer : XmlStdSerializer<Section>(Section::class.java) {

    override fun serializeXml(value: Section, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(SectionFields.SECTION, value) {
                writeXmlAttr(SectionFields.ACC_NO, accNo)
                writeXmlAttr(SectionFields.TYPE, type)
                writeXmlCollection(SectionFields.ATTRIBUTES, attributes)
                writeXmlCollection(SectionFields.LINKS, links)
                writeXmlCollection(SectionFields.FILES, files)
                writeXmlCollection(SectionFields.SUBSECTIONS, subsections)
            }
        }
    }
}