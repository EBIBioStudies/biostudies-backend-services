package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(TemporaryFolderExtension::class)
class ExtFilesTableSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun serialize() {
        val file = tempFolder.createFile("test-file.txt")
        val extFilesTable = ExtFileTable(
            NfsFile(
                filePath = "folder/test-file.txt",
                relPath = "Files/folder/test-file.txt",
                fullPath = file.absolutePath,
                file = file,
                md5 = file.md5(),
                size = file.size(),
                attributes = listOf(ExtAttribute("Type", "Data", false), ExtAttribute("Source", null, true))
            )
        )
        val expectedJson = jsonObj {
            "files" to jsonArray(
                jsonObj {
                    "fileName" to "test-file.txt"
                    "filePath" to "folder/test-file.txt"
                    "relPath" to "Files/folder/test-file.txt"
                    "fullPath" to file.absolutePath
                    "md5" to file.md5()
                    "attributes" to jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Data"
                            "reference" to false
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        },
                        jsonObj {
                            "name" to "Source"
                            "value" to null
                            "reference" to true
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        }
                    )
                    "extType" to "nfsFile"
                    "type" to "file"
                    "size" to file.size()
                }
            )
            "extType" to "filesTable"
        }.toString()

        assertThat(testInstance.writeValueAsString(extFilesTable)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
