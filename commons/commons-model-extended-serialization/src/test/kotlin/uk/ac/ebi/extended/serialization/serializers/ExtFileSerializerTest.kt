package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
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
    fun `serialize NfsFile`() {
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
    fun `serialize FireFile`() {
        val file = tempFolder.createFile("test-file.txt", "content")
        val extFile = FireFile(
            fileName = file.name,
            fireId = "fireId",
            md5 = file.md5(),
            size = file.size(),
            attributes = listOf(ExtAttribute("Type", "Data", false))
        )
        val expectedJson = jsonObj {
            "fileName" to file.name
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                }
            )
            "extType" to "fireFile"
            "type" to "file"
            "size" to file.size()
        }.toString()

        assertThat(testInstance.serialize(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
