package ac.uk.ebi.biostd.xml.deserializer

import ac.uk.ebi.biostd.xml.deserializer.stream.FileListXmlStreamDeserializer
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.redundent.kotlin.xml.xml

const val XML_FILE_LIST = "FileList.xml"

@ExtendWith(TemporaryFolderExtension::class)
class FileListXmlStreamDeserializerTest(temporaryFolder: TemporaryFolder) {
    private val testInstance = FileListXmlStreamDeserializer()
    private val xmlFile = temporaryFolder.createFile(XML_FILE_LIST)

    @BeforeEach
    fun beforeEach() {
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

        xmlFile.writeText(xml.toString())
    }

    @Test
    fun deserialize() {
        val fileList = testInstance.deserialize(xmlFile)

        assertThat(fileList.name).isEqualTo(XML_FILE_LIST)
        assertThat(fileList.referencedFiles).hasSize(2)

        assertThat(fileList.referencedFiles.first()).isEqualTo(
            File("file1.txt", attributes = listOf(Attribute("Attr1", "A"), Attribute("Attr2", "B"))))
        assertThat(fileList.referencedFiles.second()).isEqualTo(
            File("file2.txt", attributes = listOf(Attribute("Attr1", "C"), Attribute("Attr2", "D"))))
    }
}
