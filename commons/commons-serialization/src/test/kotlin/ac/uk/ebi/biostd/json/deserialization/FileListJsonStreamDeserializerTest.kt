package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.deserialization.stream.FileListJsonStreamDeserializer
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.IllegalArgumentException

@ExtendWith(TemporaryFolderExtension::class)
class FileListJsonStreamDeserializerTest(
    private val tempFolder: TemporaryFolder
) {
    private val testInstance = FileListJsonStreamDeserializer()

    @Test
    fun deserialize() {
        val jsonFileList = jsonArray(
            jsonObj {
                "path" to "file1.txt"
                "size" to 123
                "attributes" to jsonArray(
                    jsonObj {
                        "name" to "Attr1"
                        "value" to "A"
                    },
                    jsonObj {
                        "name" to "Attr2"
                        "value" to "B"
                    }
                )
            },
            jsonObj {
                "path" to "file2.txt"
                "attributes" to jsonArray(
                    jsonObj {
                        "name" to "Attr1"
                        "value" to "C"
                    },
                    jsonObj {
                        "name" to "Attr2"
                        "value" to "D"
                    }
                )
            }
        )
        val jsonFile = tempFolder.createFile("FileList.json", jsonFileList.toString())
        val fileList = testInstance.deserialize(jsonFile)

        assertThat(fileList.name).isEqualTo("FileList.json")
        assertThat(fileList.referencedFiles).hasSize(2)

        assertThat(fileList.referencedFiles.first()).isEqualTo(
            File("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B")))
        )
        assertThat(fileList.referencedFiles.second()).isEqualTo(
            File("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D")))
        )
    }

    @Test
    fun `file with empty path`() {
        val json = jsonArray(
            jsonObj { "path" to "file1.txt" },
            jsonObj { "path" to "" }
        )
        val testFile = tempFolder.createFile("invalid.json", json.toString())
        val exception = assertThrows<IllegalArgumentException> { testInstance.deserialize(testFile) }

        assertThat(exception.message).isEqualTo(REQUIRED_FILE_PATH)
    }
}
