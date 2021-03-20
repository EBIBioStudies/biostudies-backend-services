package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.constants.SectionFields

internal class SectionJsonSerializer : StdSerializer<Section>(Section::class.java) {
    override fun serialize(section: Section, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeObj {
            writeJsonString(SectionFields.ACC_NO, section.accNo)
            writeJsonString(SectionFields.TYPE, section.type)
            writeJsonArray(SectionFields.ATTRIBUTES, sectionAttributes(section))
            writeJsonArray(SectionFields.FILES, section.files)
            writeJsonArray(SectionFields.LINKS, section.links)
            writeJsonArray(SectionFields.SUBSECTIONS, section.sections)
        }
    }

    private fun sectionAttributes(section: Section): List<Attribute> = when (val fileList = section.fileList) {
        null -> section.attributes
        else -> section.attributes.plus(Attribute(SectionFields.FILE_LIST.value, "${fileList.name}.json"))
    }
}
