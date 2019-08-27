package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.deserialization.stream.FileListJsonStreamDeserializer
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

const val JSON_FILE_LIST = "FileList.json"

@ExtendWith(TemporaryFolderExtension::class)
class FileListJsonStreamDeserializerTest(temporaryFolder: TemporaryFolder) {
    private val testInstance = FileListJsonStreamDeserializer()
    private val jsonFile = temporaryFolder.createFile(JSON_FILE_LIST)

    @BeforeEach
    fun beforeEach() {
        val jsonFileList = jsonArray(jsonObj {
            "path" to "file1.txt"
            "size" to 123
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Attr1"
                    "value" to "A"
                }, jsonObj {
                    "name" to "Attr2"
                    "value" to "B"
                })
            }, jsonObj {
            "path" to "file2.txt"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Attr1"
                    "value" to "C"
                }, jsonObj {
                    "name" to "Attr2"
                    "value" to "D"
                })
            })

        jsonFile.writeText(jsonFileList.toString())
    }

    @Test
    fun deserialize() {
        val fileList = testInstance.deserialize(jsonFile)

        assertThat(fileList.name).isEqualTo(JSON_FILE_LIST)
        assertThat(fileList.referencedFiles).hasSize(2)

        assertThat(fileList.referencedFiles.first()).isEqualTo(
            File("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))))
        assertThat(fileList.referencedFiles.second()).isEqualTo(
            File("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D"))))
    }
}
