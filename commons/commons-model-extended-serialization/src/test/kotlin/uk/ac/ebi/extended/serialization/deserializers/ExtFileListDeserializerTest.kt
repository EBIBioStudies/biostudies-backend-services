package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.FireFile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.extended.serialization.constants.ExtSerializationFields.FILES
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
            FILES_URL to "$FILE_LIST_URL/S-BSST1/referencedFiles/file-list"
        }.toString()

        val extFileList = testInstance.deserialize<ExtFileList>(json)
        assertThat(extFileList.fileName).isEqualTo("file-list")
        assertThat(extFileList.filesUrl).isEqualTo("$FILE_LIST_URL/S-BSST1/referencedFiles/file-list")
        assertThat(extFileList.files).isEmpty()
    }

    @Test
    fun `deserialize with files`() {
        val json = jsonObj {
            FILE_NAME to "file-list"
            FILES_URL to "$FILE_LIST_URL/S-BSST1/referencedFiles/file-list"
            FILES to jsonArray(
                jsonObj {
                    "fileName" to "test-file.txt"
                    "filePath" to "folder/test-file.txt"
                    "relPath" to "Files/folder/test-file.txt"
                    "fireId" to "fireId"
                    "attributes" to jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Data"
                        }
                    )
                    "type" to "file"
                    "size" to 10
                    "md5" to "fireFileMd5"
                    "extType" to "fireFile"
                }
            )
        }.toString()

        val extFileList = testInstance.deserialize<ExtFileList>(json)
        assertThat(extFileList.fileName).isEqualTo("file-list")
        assertThat(extFileList.filesUrl).isEqualTo("$FILE_LIST_URL/S-BSST1/referencedFiles/file-list")
        val extFile = extFileList.files.first() as FireFile
        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.fireId).isEqualTo("fireId")
        assertThat(extFile.md5).isEqualTo("fireFileMd5")
        assertThat(extFile.size).isEqualTo(10)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first()).isEqualTo(ExtAttribute("Type","Data"))
    }
}
