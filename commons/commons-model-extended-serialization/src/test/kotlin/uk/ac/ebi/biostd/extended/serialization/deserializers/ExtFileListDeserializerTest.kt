package uk.ac.ebi.biostd.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.biostd.dsl.json.jsonObj
import uk.ac.ebi.biostd.extended.model.ExtFileList
import uk.ac.ebi.biostd.extended.serialization.constants.ExtSerializationFields.FILE
import uk.ac.ebi.biostd.extended.serialization.constants.ExtSerializationFields.FILES_URL
import uk.ac.ebi.biostd.extended.serialization.constants.ExtSerializationFields.FILE_NAME
import uk.ac.ebi.biostd.extended.serialization.serializers.FILE_LIST_URL
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.extended.serialization.service.createExtFileList

class ExtFileListDeserializerTest {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val file = createExtFileList()
        val json =
            jsonObj {
                FILE_NAME to "file-list"
                FILES_URL to "$FILE_LIST_URL/S-BSST1/referencedFiles/file-list"
                FILE to file.absolutePath
            }.toString()

        val extFileList = testInstance.readValue<ExtFileList>(json)
        assertThat(extFileList.filePath).isEqualTo("file-list")
        assertThat(extFileList.filesUrl).isEqualTo("$FILE_LIST_URL/S-BSST1/referencedFiles/file-list")
        assertThat(extFileList.file).isEqualTo(file)
    }
}
