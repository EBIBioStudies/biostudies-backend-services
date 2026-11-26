package uk.ac.ebi.biostd.json.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.ac.ebi.biostd.json.common.writeJsonArray
import uk.ac.ebi.biostd.json.common.writeJsonString
import uk.ac.ebi.biostd.json.common.writeObj
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.constants.SectionFields
import uk.ac.ebi.biostd.model.constants.SectionFields.FILE_LIST
import uk.ac.ebi.biostd.model.constants.SectionFields.LINK_LIST

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
