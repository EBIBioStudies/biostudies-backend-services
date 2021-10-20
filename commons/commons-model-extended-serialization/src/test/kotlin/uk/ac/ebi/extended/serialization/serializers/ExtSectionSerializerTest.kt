package uk.ac.ebi.extended.serialization.serializers

import arrow.core.Either
import ebi.ac.uk.dsl.json.JsonNull
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonNull
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.size
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

        val fileNfs = tempFolder.createFile("fileNfs.txt")
        val pageTabFireFile = FireFile("fileFileName", "filePath", "relPath", "fireId", "fileMd5", 1, listOf())
        val pageTabFireDirectory = FireDirectory("dirFileName", "filePath", "relPath", "dirMd5", 2, listOf())

        val allInOneSection = ExtSection(
            accNo = "SECT-001",
            type = "Study",
            fileList = ExtFileList(
                "file-list",
                listOf(NfsFile("ref-file.txt", "filePath", "relPath", "fullPath", referencedFile)),
                pageTabFiles = listOf(
                    pageTabFireFile,
                    pageTabFireDirectory,
                    NfsFile(fileNfs.name, "filePath", "relPath", "fullPath", fileNfs)
                )
            ),
            attributes = listOf(ExtAttribute("Title", "Test Section")),
            sections = listOf(
                Either.left(ExtSection(type = "Exp")),
                Either.right(ExtSectionTable(listOf(ExtSection(type = "Data"))))
            ),
            files = listOf(
                Either.left(NfsFile("section-file.txt", "filePath", "relPath", "fullPath", sectionFile)),
                Either.right(
                    ExtFileTable(
                        listOf(
                            NfsFile(
                                "section-file-table.txt",
                                "filePath",
                                "relPath",
                                "fullPath",
                                sectionFilesTable
                            )
                        )
                    )
                )
            ),
            links = listOf(
                Either.left(ExtLink(url = "https://mylink.org")),
                Either.right(ExtLinkTable(listOf(ExtLink(url = "https://mytable.org"))))
            )
        )
        val expectedJson = jsonObj {
            "accNo" to "SECT-001"
            "type" to "Study"
            "fileList" to jsonObj {
                "fileName" to "file-list"
                "filesUrl" to "/submissions/extended/S-BSST1/referencedFiles/file-list"
                "pageTabFiles" to jsonArray(
                    jsonObj {
                        "fileName" to pageTabFireFile.fileName
                        "filePath" to pageTabFireFile.filePath
                        "relPath" to "relPath"
                        "fireId" to "fireId"
                        "attributes" to jsonArray()
                        "extType" to "fireFile"
                        "type" to "file"
                        "md5" to pageTabFireFile.md5
                        "size" to pageTabFireFile.size
                    },
                    jsonObj {
                        "fileName" to pageTabFireDirectory.fileName
                        "filePath" to "filePath"
                        "relPath" to "relPath"
                        "attributes" to jsonArray()
                        "extType" to "fireDirectory"
                        "type" to "directory"
                        "md5" to pageTabFireDirectory.md5
                        "size" to pageTabFireDirectory.size
                    },
                    jsonObj {
                        "fileName" to fileNfs.name
                        "filePath" to "filePath"
                        "relPath" to "relPath"
                        "fullPath" to "fullPath"
                        "file" to fileNfs.absolutePath
                        "attributes" to jsonArray()
                        "extType" to "nfsFile"
                        "type" to "file"
                        "size" to fileNfs.size()
                    }
                )
            }
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Title"
                    "value" to "Test Section"
                    "reference" to false
                }
            )

            "sections" to jsonArray(
                jsonObj {
                    "accNo" to jsonNull
                    "type" to "Exp"
                    "fileList" to jsonNull
                    "attributes" to jsonArray()
                    "sections" to jsonArray()
                    "files" to jsonArray()
                    "links" to jsonArray()
                    "extType" to "section"
                },
                jsonObj {
                    "sections" to jsonArray(
                        jsonObj {
                            "accNo" to jsonNull
                            "type" to "Data"
                            "fileList" to jsonNull
                            "attributes" to jsonArray()
                            "sections" to jsonArray()
                            "files" to jsonArray()
                            "links" to jsonArray()
                            "extType" to "section"
                        }
                    )
                    "extType" to "sectionsTable"
                }
            )

            "files" to jsonArray(
                jsonObj {
                    "fileName" to "section-file.txt"
                    "filePath" to "filePath"
                    "relPath" to "relPath"
                    "fullPath" to "fullPath"
                    "file" to sectionFile.absolutePath
                    "attributes" to jsonArray()
                    "extType" to "nfsFile"
                    "type" to "file"
                    "size" to 0
                },
                jsonObj {
                    "files" to jsonArray(
                        jsonObj {
                            "fileName" to "section-file-table.txt"
                            "filePath" to "filePath"
                            "relPath" to "relPath"
                            "fullPath" to "fullPath"
                            "file" to sectionFilesTable.absolutePath
                            "attributes" to jsonArray()
                            "extType" to "nfsFile"
                            "type" to "file"
                            "size" to 0
                        }
                    )
                    "extType" to "filesTable"
                }
            )

            "links" to jsonArray(
                jsonObj {
                    "url" to "https://mylink.org"
                    "attributes" to jsonArray()
                    "extType" to "link"
                },
                jsonObj {
                    "links" to jsonArray(
                        jsonObj {
                            "url" to "https://mytable.org"
                            "attributes" to jsonArray()
                            "extType" to "link"
                        }
                    )
                    "extType" to "linksTable"
                }
            )

            "extType" to "section"
        }.toString()

        ExtSectionSerializer.parentAccNo = "S-BSST1"
        assertThat(testInstance.serialize(allInOneSection)).isEqualToIgnoringWhitespace(expectedJson)
    }
}
