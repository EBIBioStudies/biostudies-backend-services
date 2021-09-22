package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.serialize

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `serialize nfs file`() {
        val file = tempFolder.createFile("test-file.txt", "content")
        val extFile = NfsFile(
            file = file,
            fileName = "test/path/test-file.txt",
            attributes = listOf(ExtAttribute("Type", "Data", false))
        )
        val expectedJson = jsonObj {
            "fileName" to "test-file.txt"
            "path" to "test/path/test-file.txt"
            "file" to file.absolutePath
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                }
            )
            "extType" to "nfsFile"
            "type" to "file"
            "size" to 7
        }.toString()

        assertThat(testInstance.serialize(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize fire file`() {
        val extFile = FireFile(
            fileName = "fire-file",
            fireId = "fireId",
            md5 = "fireFileMd5",
            size = 1,
            attributes = listOf(ExtAttribute("Type", "Data", false))
        )
        val expectedJson = jsonObj {
            "fileName" to "fire-file"
            "fireId" to "fireId"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                }
            )
            "extType" to "fireFile"
            "type" to "file"
            "md5" to "fireFileMd5"
            "size" to 1
        }.toString()

        assertThat(testInstance.serialize(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize fire directory`() {
        val extFile = FireDirectory(
            fileName = "fire-directory",
            md5 = "fireDirMd5",
            size = 2,
            attributes = listOf(ExtAttribute("Type", "Data", false))
        )
        val expectedJson = jsonObj {
            "fileName" to "fire-directory"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                }
            )
            "extType" to "fireDirectory"
            "type" to "directory"
            "md5" to "fireDirMd5"
            "size" to 2
        }.toString()

        assertThat(testInstance.serialize(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
