package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
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
class ExtFilesTableDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val file = tempFolder.createFile("test-file.txt")
        val json =
            jsonObj {
                "files" to
                    jsonArray(
                        jsonObj {
                            "fileName" to "test-file.txt"
                            "filePath" to "folder/test-file.txt"
                            "relPath" to "Files/folder/test-file.txt"
                            "fullPath" to file.absolutePath
                            "md5" to file.md5()
                            "size" to file.size()
                            "file" to file.absolutePath
                            "attributes" to
                                jsonArray(
                                    jsonObj {
                                        "name" to "Type"
                                        "value" to "Data"
                                    },
                                )
                            "extType" to "nfsFile"
                            "type" to "file"
                            "size" to file.size()
                        },
                    )
                "extType" to "filesTable"
            }.toString()

        val extFilesTable = testInstance.readValue<ExtFileTable>(json)
        assertThat(extFilesTable.files).hasSize(1)

        val extFile = extFilesTable.files.first() as NfsFile
        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.fullPath).isEqualTo(file.absolutePath)
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }
}
