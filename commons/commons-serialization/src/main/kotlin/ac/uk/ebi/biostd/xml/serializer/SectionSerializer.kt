package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlAttr
import ac.uk.ebi.biostd.xml.common.writeXmlCollection
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.model.extensions.fileListName

class SectionSerializer : XmlStdSerializer<Section>(Section::class.java) {
    override fun serializeXml(value: Section, gen: ToXmlGenerator, provider: SerializerProvider) {
        addFileListExt(value)
        with(gen) {
            writeXmlObj(SectionFields.SECTION) {
                writeXmlAttr(SectionFields.ACC_NO, value.accNo)
                writeXmlAttr(SectionFields.TYPE, value.type)
                writeXmlCollection(SectionFields.ATTRIBUTES, value.attributes)
                writeXmlCollection(SectionFields.LINKS, value.links)
                writeXmlCollection(SectionFields.FILES, value.files)
                writeXmlCollection(SectionFields.SUBSECTIONS, value.sections)
            }
        }
    }

    private fun addFileListExt(section: Section) =
        section.fileList?.let { section.fileListName = "${it.name}.xml" }
}
