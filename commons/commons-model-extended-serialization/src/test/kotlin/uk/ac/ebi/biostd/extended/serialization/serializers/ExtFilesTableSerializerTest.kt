package uk.ac.ebi.biostd.extended.serialization.serializers

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.dsl.json.jsonArray
import uk.ac.ebi.biostd.dsl.json.jsonObj
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.NfsFile
import uk.ac.ebi.biostd.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.io.ext.size

@ExtendWith(TemporaryFolderExtension::class)
class ExtFilesTableSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun serialize() {
        val file = tempFolder.createFile("test-file.txt")
        val extFilesTable =
            ExtFileTable(
                NfsFile(
                    filePath = "folder/test-file.txt",
                    relPath = "Files/folder/test-file.txt",
                    fullPath = file.absolutePath,
                    file = file,
                    md5 = file.md5(),
                    size = file.size(),
                    attributes = listOf(ExtAttribute("Type", "Data", false), ExtAttribute("Source", null, true)),
                ),
            )
        val expectedJson =
            jsonObj {
                "files" to
                    jsonArray(
                        jsonObj {
                            "fileName" to "test-file.txt"
                            "filePath" to "folder/test-file.txt"
                            "relPath" to "Files/folder/test-file.txt"
                            "fullPath" to file.absolutePath
                            "md5" to file.md5()
                            "attributes" to
                                jsonArray(
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
                                    },
                                )
                            "extType" to "nfsFile"
                            "type" to "file"
                            "size" to file.size()
                        },
                    )
                "extType" to "filesTable"
            }.toString()

        assertThat(testInstance.writeValueAsString(extFilesTable)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
