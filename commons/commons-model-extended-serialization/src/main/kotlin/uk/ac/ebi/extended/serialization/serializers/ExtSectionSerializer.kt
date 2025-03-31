package uk.ac.ebi.extended.serialization.serializers

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSection
import org.springframework.web.util.UriUtils.encodePath
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ACC_NO
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.ATTRIBUTES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.EXT_TYPE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES_URL
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINKS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.LINK_LIST
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.PAGE_TAB_FILES
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.SECTIONS
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.TYPE
import uk.ac.ebi.extended.serialization.constants.ExtType
import java.nio.charset.StandardCharsets.UTF_8

const val FILE_LIST_URL = "submissions/extended"

class ExtSectionSerializer : JsonSerializer<ExtSection>() {
    override fun serialize(
        section: ExtSection,
        gen: JsonGenerator,
        serializers: SerializerProvider,
    ) {
        serialize(section, gen)
    }

    private fun serialize(
        section: ExtSection,
        gen: JsonGenerator,
    ) {
        gen.writeStartObject()
        gen.writeStringField(ACC_NO, section.accNo)
        gen.writeStringField(TYPE, section.type)
        section.fileList?.let { writeFileList(it, gen) } ?: gen.writeNullField(FILE_LIST)
        section.linkList?.let { writeLinkList(it, gen) } ?: gen.writeNullField(LINK_LIST)
        gen.writeObjectField(ATTRIBUTES, section.attributes)
        writeEitherList(SECTIONS, section.sections, gen)
        writeEitherList(FILES, section.files, gen)
        writeEitherList(LINKS, section.links, gen)
        gen.writeStringField(EXT_TYPE, ExtType.Section.type)
        gen.writeEndObject()
    }

    private fun writeFileList(
        fileList: ExtFileList,
        gen: JsonGenerator,
    ) {
        gen.writeObjectFieldStart(FILE_LIST)
        gen.writeStringField(FILE_NAME, fileList.filePath)
        gen.writeStringField(FILES_URL, fileUrl(fileList))
        gen.writeStringField(FILE, fileList.file.absolutePath)
        gen.writeObjectField(PAGE_TAB_FILES, fileList.pageTabFiles)
        gen.writeEndObject()
    }

    private fun writeLinkList(
        linkList: ExtLinkList,
        gen: JsonGenerator,
    ) {
        gen.writeObjectFieldStart(LINK_LIST)
        gen.writeStringField(FILE_NAME, linkList.filePath)
        gen.writeStringField(FILE, linkList.file.absolutePath)
        gen.writeObjectField(PAGE_TAB_FILES, linkList.pageTabFiles)
        gen.writeEndObject()
    }

    private fun fileUrl(fileList: ExtFileList): String =
        encodePath("/$FILE_LIST_URL/$parentAccNo/referencedFiles/${fileList.filePath}", UTF_8)

    private fun writeEitherList(
        fieldName: String,
        list: List<Either<*, *>>,
        gen: JsonGenerator,
    ) {
        gen.writeArrayFieldStart(fieldName)
        list.forEach { gen.writeObject(it) }
        gen.writeEndArray()
    }

    companion object {
        var parentAccNo = ""
    }
}
