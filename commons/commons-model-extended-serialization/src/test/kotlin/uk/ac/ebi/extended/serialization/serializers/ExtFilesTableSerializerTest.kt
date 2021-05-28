package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.serialize

@ExtendWith(TemporaryFolderExtension::class)
class ExtFilesTableSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun serialize() {
        val file = tempFolder.createFile("test-file.txt")
        val extFilesTable = ExtFileTable(
            NfsFile(
                file = file,
                fileName = "test/path/test-file.txt",
                attributes = listOf(ExtAttribute("Type", "Data", false))
            )
        )
        val expectedJson = jsonObj {
            "files" to jsonArray(
                jsonObj {
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
                    "size" to 0
                }
            )
            "extType" to "filesTable"
        }.toString()

        assertThat(testInstance.serialize(extFilesTable)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
