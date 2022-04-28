package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.extended.serialization.service.createFileList
import kotlin.test.assertNotNull

@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `deserialize basic`() {
        val json = jsonObj {
            "type" to "Study"
            "extType" to "section"
        }.toString()

        val extSection = testInstance.readValue<ExtSection>(json)
        assertThat(extSection.accNo).isNull()
        assertThat(extSection.fileList).isNull()
        assertThat(extSection.files).isEmpty()
        assertThat(extSection.links).isEmpty()
        assertThat(extSection.sections).isEmpty()
        assertThat(extSection.type).isEqualTo("Study")
    }

    @Test
    fun `deserialize all in one`() {
        val sectionFile = tempFolder.createFile("section-file.txt")
        val sectionFilesTable = tempFolder.createFile("section-file-table.txt")
        val json = jsonObj {
            "accNo" to "SECT-001"
            "type" to "Study"
            "fileList" to jsonObj {
                "fileName" to "file-list.json"
                "path" to "file-list.json"
                "file" to createFileList(emptyList()).absolutePath
                "filesUrl" to "submissions/extended/S-BSST1/referencedFiles/file-list"
            }
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Title"
                    "value" to "Test Section"
                }
            )

            "sections" to jsonArray(
                jsonObj {
                    "type" to "Exp"
                    "extType" to "section"
                },
                jsonObj {
                    "sections" to jsonArray(
                        jsonObj {
                            "type" to "Data"
                            "extType" to "section"
                        }
                    )
                    "extType" to "sectionsTable"
                }
            )

            "files" to jsonArray(
                jsonObj {
                    "fileName" to "section-file-inner-folders.txt"
                    "filePath" to "folder/section-file-inner-folders.txt"
                    "relPath" to "Files/folder/section-file-inner-folders.txt"
                    "md5" to "abc-md5"
                    "size" to 55
                    "fullPath" to sectionFile.absolutePath
                    "extType" to "nfsFile"
                },
                jsonObj {
                    "files" to jsonArray(
                        jsonObj {
                            "fileName" to "section-file-table.txt"
                            "filePath" to "folder/section-file-table.txt"
                            "relPath" to "Files/folder/section-file-table.txt"
                            "fullPath" to sectionFilesTable.absolutePath
                            "md5" to "abc-md5"
                            "size" to 55
                            "extType" to "nfsFile"
                        }
                    )
                    "extType" to "filesTable"
                }
            )

            "links" to jsonArray(
                jsonObj {
                    "url" to "https://simple-link.net"
                    "extType" to "link"
                },
                jsonObj {
                    "links" to jsonArray(
                        jsonObj {
                            "url" to "https://table-link.net"
                            "extType" to "link"
                        }
                    )
                    "extType" to "linksTable"
                }
            )

            "extType" to "section"
        }.toString()

        val extSection = testInstance.readValue<ExtSection>(json)
        assertThat(extSection.accNo).isEqualTo("SECT-001")
        assertThat(extSection.type).isEqualTo("Study")
        assertThat(extSection.attributes).hasSize(1)
        assertThat(extSection.attributes.first().name).isEqualTo("Title")
        assertThat(extSection.attributes.first().value).isEqualTo("Test Section")

        val extFileList = extSection.fileList
        assertNotNull(extFileList)
        assertThat(extFileList.filePath).isEqualTo("file-list.json")
        assertThat(extFileList.filesUrl).isEqualTo("submissions/extended/S-BSST1/referencedFiles/file-list")
        assertThat(extFileList.file).hasContent("[]")

        val innerSections = extSection.sections
        assertThat(innerSections).hasSize(2)

        val innerSection = innerSections.first()
        assertThat(innerSection.isLeft()).isTrue
        innerSection.ifLeft {
            assertThat(it.type).isEqualTo("Exp")
        }

        val innerSectionsTable = innerSections.second()
        assertThat(innerSectionsTable.isRight()).isTrue
        innerSectionsTable.ifRight {
            assertThat(it.sections).hasSize(1)
            assertThat(it.sections.first().type).isEqualTo("Data")
        }

        val extFiles = extSection.files
        assertThat(extFiles).hasSize(2)

        val extFile = extFiles.first()
        assertThat(extFile.isLeft()).isTrue
        extFile.ifLeft {
            it as NfsFile
            assertThat(it.fileName).isEqualTo("section-file-inner-folders.txt")
            assertThat(it.filePath).isEqualTo("folder/section-file-inner-folders.txt")
            assertThat(it.relPath).isEqualTo("Files/folder/section-file-inner-folders.txt")
            assertThat(it.fullPath).isEqualTo(sectionFile.absolutePath)
            assertThat(it.file).isEqualTo(sectionFile)
        }

        val extFilesTable = extFiles.second()
        assertThat(extFilesTable.isRight()).isTrue
        extFilesTable.ifRight {
            assertThat(it.files).hasSize(1)

            val filesTableFile = it.files.first() as NfsFile
            assertThat(filesTableFile.fileName).isEqualTo("section-file-table.txt")
            assertThat(filesTableFile.filePath).isEqualTo("folder/section-file-table.txt")
            assertThat(filesTableFile.relPath).isEqualTo("Files/folder/section-file-table.txt")
            assertThat(filesTableFile.fullPath).isEqualTo(sectionFilesTable.absolutePath)
            assertThat(filesTableFile.file).isEqualTo(sectionFilesTable)
        }

        val extLinks = extSection.links
        assertThat(extLinks).hasSize(2)

        val extLink = extLinks.first()
        assertThat(extLink.isLeft()).isTrue
        extLink.ifLeft {
            assertThat(it.url).isEqualTo("https://simple-link.net")
        }

        val extLinkTable = extLinks.second()
        assertThat(extLinkTable.isRight()).isTrue
        extLinkTable.ifRight {
            assertThat(it.links).hasSize(1)
            assertThat(it.links.first().url).isEqualTo("https://table-link.net")
        }
    }
}
