package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import ac.uk.ebi.biostd.xml.deserializer.stream.FileListXmlStreamDeserializer
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
import org.redundent.kotlin.xml.xml
import java.lang.IllegalArgumentException

@ExtendWith(TemporaryFolderExtension::class)
class FileListXmlStreamDeserializerTest(
    private val tempFolder: TemporaryFolder
) {
    private val testInstance = FileListXmlStreamDeserializer()

    @Test
    fun deserialize() {
        val xml = xml("files") {
            "file" {
                "path" { -"file1.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"Attr1" }
                        "value" { -"A" }
                    }
                    "attribute" {
                        "name" { -"Attr2" }
                        "value" { -"B" }
                    }
                }
            }
            "file" {
                "path" { -"file2.txt" }
                "attributes" {
                    "attribute" {
                        "name" { -"Attr1" }
                        "value" { -"C" }
                    }
                    "attribute" {
                        "name" { -"Attr2" }
                        "value" { -"D" }
                    }
                }
            }
        }
        val xmlFile = tempFolder.createFile("FileList.xml", xml.toString())
        val fileList = testInstance.deserialize(xmlFile)

        assertThat(fileList.name).isEqualTo("FileList.xml")
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
        val xml = xml("files") {
            "file" {
                "path" { -"file1.txt" }
            }
            "file" {
                "path" { -"" }
            }
        }
        val testFile = tempFolder.createFile("invalid.xml", xml.toString())
        val exception = assertThrows<IllegalArgumentException> { testInstance.deserialize(testFile) }

        assertThat(exception.message).isEqualTo(REQUIRED_FILE_PATH)
    }
}
