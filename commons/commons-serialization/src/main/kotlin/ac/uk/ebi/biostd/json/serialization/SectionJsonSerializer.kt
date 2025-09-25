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
import ebi.ac.uk.model.constants.SectionFields.FILE_LIST
import ebi.ac.uk.model.constants.SectionFields.LINK_LIST

internal class SectionJsonSerializer : StdSerializer<Section>(Section::class.java) {
    override fun serialize(
        section: Section,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeObj {
            writeJsonString(SectionFields.ACC_NO, section.accNo)
            writeJsonString(SectionFields.TYPE, section.type)
            writeJsonArray(SectionFields.ATTRIBUTES, sectionAttributes(section))
            writeJsonArray(SectionFields.FILES, section.files)
            writeJsonArray(SectionFields.LINKS, section.links)
            writeJsonArray(SectionFields.SUBSECTIONS, section.sections)
        }
    }

    private fun sectionAttributes(section: Section) =
        section.attributes.plus(
            listOfNotNull(
                section.fileList?.let { Attribute(FILE_LIST.value, "${it.name}.json") },
                section.linkList?.let { Attribute(LINK_LIST.value, "${it.name}.json") },
            ),
        )
}
