package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFileList
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES_URL
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.extended.serialization.serializers.FILE_LIST_URL
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize

class ExtFileListDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val json = jsonObj {
            FILE_NAME to "file-list"
            FILES_URL to "$FILE_LIST_URL/S-BSST1/fileList/file-list/files"
        }.toString()

        val extFileList = testInstance.deserialize<ExtFileList>(json)
        assertThat(extFileList.fileName).isEqualTo("file-list")
        assertThat(extFileList.filesUrl).isEqualTo("$FILE_LIST_URL/S-BSST1/fileList/file-list/files")
        assertThat(extFileList.files).isEmpty()
    }
}