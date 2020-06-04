package uk.ac.ebi.extended.serialization.serializers

import arrow.core.Either
import ebi.ac.uk.dsl.json.JsonNull
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonNull
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.serialize

@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionSerializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `serialize basic section`() {
        val extSection = ExtSection(type = "Study")
        val expectedJson = jsonObj {
            "accNo" to JsonNull
            "type" to "Study"
            "fileList" to JsonNull
            "attributes" to jsonArray()
            "sections" to jsonArray()
            "files" to jsonArray()
            "links" to jsonArray()
            "extType" to "section"
        }.toString()

        assertThat(testInstance.serialize(extSection)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize all in one`() {
        val referencedFile = tempFolder.createFile("ref-file.txt")
        val sectionFile = tempFolder.createFile("section-file.txt")
        val sectionFilesTable = tempFolder.createFile("section-file-table.txt")
        val allInOneSection = ExtSection(
            accNo = "SECT-001",
            type = "Study",
            fileList = ExtFileList("file-list.json", listOf(ExtFile("ref-file.txt", referencedFile))),
            attributes = listOf(ExtAttribute("Title", "Test Section")),
            sections = listOf(
                Either.left(ExtSection(type ="Exp")),
                Either.right(ExtSectionTable(listOf(ExtSection(type = "Data"))))),
            files = listOf(
                Either.left(ExtFile("section-file.txt", sectionFile)),
                Either.right(ExtFileTable(listOf(ExtFile("section-file-table.txt", sectionFilesTable))))),
            links = listOf(
                Either.left(ExtLink(url = "http://mylink.org")),
                Either.right(ExtLinkTable(listOf(ExtLink(url = "http://mytable.org")))))
        )
        val expectedJson = jsonObj {
            "accNo" to "SECT-001"
            "type" to "Study"
            "fileList" to jsonObj {
                "fileName" to "file-list.json"
                "files" to jsonArray(jsonObj {
                    "fileName" to "ref-file.txt"
                    "file" to referencedFile.absolutePath
                    "attributes" to jsonArray()
                    "extType" to "file"
                })
            }
            "attributes" to jsonArray(jsonObj {
                "name" to "Title"
                "value" to "Test Section"
                "reference" to false
            })

            "sections" to jsonArray(jsonObj {
                "accNo" to jsonNull
                "type" to "Exp"
                "fileList" to jsonNull
                "attributes" to jsonArray()
                "sections" to jsonArray()
                "files" to jsonArray()
                "links" to jsonArray()
                "extType" to "section"
            }, jsonObj {
                "sections" to jsonArray(jsonObj {
                    "accNo" to jsonNull
                    "type" to "Data"
                    "fileList" to jsonNull
                    "attributes" to jsonArray()
                    "sections" to jsonArray()
                    "files" to jsonArray()
                    "links" to jsonArray()
                    "extType" to "section"
                })
                "extType" to "sectionsTable"
            })

            "files" to jsonArray(jsonObj {
                "fileName" to "section-file.txt"
                "file" to sectionFile.absolutePath
                "attributes" to jsonArray()
                "extType" to "file"
            }, jsonObj {
                "files" to jsonArray(jsonObj {
                    "fileName" to "section-file-table.txt"
                    "file" to sectionFilesTable.absolutePath
                    "attributes" to jsonArray()
                    "extType" to "file"
                })
                "extType" to "filesTable"
            })

            "links" to jsonArray(jsonObj {
                "url" to "http://mylink.org"
                "attributes" to jsonArray()
                "extType" to "link"
            }, jsonObj {
                "links" to jsonArray(jsonObj {
                    "url" to "http://mytable.org"
                    "attributes" to jsonArray()
                    "extType" to "link"
                })
                "extType" to "linksTable"
            })

            "extType" to "section"
        }.toString()

        assertThat(testInstance.serialize(allInOneSection)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
