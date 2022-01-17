package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
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
class ExtFileSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `serialize nfs file`() {
        val file = tempFolder.createFile("nfs-file.txt")
        val extFile = NfsFile(
            filePath = "folder/nfs-file.txt",
            relPath = "Files/folder/nfs-file.txt",
            file = file,
            fullPath = file.absolutePath,
            size = file.size(),
            md5 = file.md5(),
            attributes = listOf(ExtAttribute("Type", "Data", false), ExtAttribute("Source", null, true))
        )
        val expectedJson = jsonObj {
            "fileName" to "nfs-file.txt"
            "filePath" to "folder/nfs-file.txt"
            "relPath" to "Files/folder/nfs-file.txt"
            "fullPath" to file.absolutePath
            "md5" to file.md5()
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                },
                jsonObj {
                    "name" to "Source"
                    "value" to null
                    "reference" to true
                }
            )
            "extType" to "nfsFile"
            "type" to "file"
            "size" to file.size()
        }.toString()

        assertThat(testInstance.writeValueAsString(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize fire file`() {
        val extFile = FireFile(
            filePath = "folder/fire-file.txt",
            relPath = "Files/folder/fire-file.txt",
            fireId = "fireId",
            md5 = "fireFileMd5",
            size = 13,
            attributes = listOf(ExtAttribute("Type", "Data", false), ExtAttribute("Source", null, true))
        )
        val expectedJson = jsonObj {
            "fileName" to "fire-file.txt"
            "filePath" to "folder/fire-file.txt"
            "relPath" to "Files/folder/fire-file.txt"
            "fireId" to "fireId"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                },
                jsonObj {
                    "name" to "Source"
                    "value" to null
                    "reference" to true
                }
            )
            "extType" to "fireFile"
            "type" to "file"
            "md5" to "fireFileMd5"
            "size" to 13
        }.toString()

        assertThat(testInstance.writeValueAsString(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize fire directory`() {
        val extFile = FireDirectory(
            filePath = "folder/fire-directory.txt",
            relPath = "Files/folder/fire-directory.txt",
            md5 = "fireDirMd5",
            size = 12,
            attributes = listOf(ExtAttribute("Type", "Data", false), ExtAttribute("Source", null, true))
        )
        val expectedJson = jsonObj {
            "fileName" to "fire-directory.txt"
            "filePath" to "folder/fire-directory.txt"
            "relPath" to "Files/folder/fire-directory.txt"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                    "reference" to false
                },
                jsonObj {
                    "name" to "Source"
                    "value" to null
                    "reference" to true
                }
            )
            "extType" to "fireDirectory"
            "type" to "directory"
            "md5" to "fireDirMd5"
            "size" to 12
        }.toString()

        assertThat(testInstance.writeValueAsString(extFile)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
