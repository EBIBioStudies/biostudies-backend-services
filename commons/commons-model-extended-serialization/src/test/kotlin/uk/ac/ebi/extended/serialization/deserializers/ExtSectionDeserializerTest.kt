package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `deserialize basic`() {
        val json = jsonObj {
            "type" to "Study"
            "extType" to "section"
        }.toString()

        val extSection = testInstance.deserialize<ExtSection>(json)
        assertNull(extSection.accNo)
        assertNull(extSection.fileList)
        assertThat(extSection.files).isEmpty()
        assertThat(extSection.links).isEmpty()
        assertThat(extSection.sections).isEmpty()
        assertThat(extSection.type).isEqualTo("Study")
    }

    @Test
    fun `deserialize all in one`() {
        val referencedFile = tempFolder.createFile("ref-file.txt")
        val sectionFile = tempFolder.createFile("section-file.txt")
        val sectionFilesTable = tempFolder.createFile("section-file-table.txt")
        val json = jsonObj {
            "accNo" to "SECT-001"
            "type" to "Study"
            "fileList" to jsonObj {
                "fileName" to "file-list.json"
                "files" to jsonArray(jsonObj {
                    "fileName" to "ref-file.txt"
                    "file" to referencedFile.absolutePath
                    "extType" to "file"
                })
            }
            "attributes" to jsonArray(jsonObj {
                "name" to "Title"
                "value" to "Test Section"
            })

            "sections" to jsonArray(jsonObj {
                "type" to "Exp"
                "extType" to "section"
            }, jsonObj {
                "sections" to jsonArray(jsonObj {
                    "type" to "Data"
                    "extType" to "section"
                })
                "extType" to "sectionsTable"
            })

            "files" to jsonArray(jsonObj {
                "fileName" to "section-file.txt"
                "file" to sectionFile.absolutePath
                "extType" to "file"
            }, jsonObj {
                "files" to jsonArray(jsonObj {
                    "fileName" to "section-file-table.txt"
                    "file" to sectionFilesTable.absolutePath
                    "extType" to "file"
                })
                "extType" to "filesTable"
            })

            "links" to jsonArray(jsonObj {
                "url" to "http://simple-link.net"
                "extType" to "link"
            }, jsonObj {
                "links" to jsonArray(jsonObj {
                    "url" to "http://table-link.net"
                    "extType" to "link"
                })
                "extType" to "linksTable"
            })

            "extType" to "section"
        }.toString()

        val extSection = testInstance.deserialize<ExtSection>(json)
        assertThat(extSection.accNo).isEqualTo("SECT-001")
        assertThat(extSection.type).isEqualTo("Study")
        assertThat(extSection.attributes).hasSize(1)
        assertThat(extSection.attributes.first().name).isEqualTo("Title")
        assertThat(extSection.attributes.first().value).isEqualTo("Test Section")

        val extFileList = extSection.fileList
        assertNotNull(extFileList)
        assertThat(extFileList.fileName).isEqualTo("file-list.json")
        assertThat(extFileList.files).hasSize(1)
        assertThat(extFileList.files.first().file).isEqualTo(referencedFile)
        assertThat(extFileList.files.first().fileName).isEqualTo("ref-file.txt")

        val innerSections = extSection.sections
        assertThat(innerSections).hasSize(2)

        val innerSection = innerSections.first()
        assertTrue(innerSection.isLeft())
        innerSection.ifLeft {
            assertThat(it.type).isEqualTo("Exp")
        }

        val innerSectionsTable = innerSections.second()
        assertTrue(innerSectionsTable.isRight())
        innerSectionsTable.ifRight {
            assertThat(it.sections).hasSize(1)
            assertThat(it.sections.first().type).isEqualTo("Data")
        }

        val extFiles = extSection.files
        assertThat(extFiles).hasSize(2)

        val extFile = extFiles.first()
        assertTrue(extFile.isLeft())
        extFile.ifLeft {
            assertThat(it.file).isEqualTo(sectionFile)
            assertThat(it.fileName).isEqualTo("section-file.txt")
        }

        val extFilesTable = extFiles.second()
        assertTrue(extFilesTable.isRight())
        extFilesTable.ifRight {
            assertThat(it.files).hasSize(1)
            assertThat(it.files.first().file).isEqualTo(sectionFilesTable)
            assertThat(it.files.first().fileName).isEqualTo("section-file-table.txt")
        }

        val extLinks = extSection.links
        assertThat(extLinks).hasSize(2)

        val extLink = extLinks.first()
        assertTrue(extLink.isLeft())
        extLink.ifLeft {
            assertThat(it.url).isEqualTo("http://simple-link.net")
        }

        val extLinkTable = extLinks.second()
        assertTrue(extLinkTable.isRight())
        extLinkTable.ifRight {
            assertThat(it.links).hasSize(1)
            assertThat(it.links.first().url).isEqualTo("http://table-link.net")
        }
    }
}
