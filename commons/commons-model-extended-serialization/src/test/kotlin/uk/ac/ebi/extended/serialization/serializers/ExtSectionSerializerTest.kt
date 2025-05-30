package uk.ac.ebi.extended.serialization.serializers

import ebi.ac.uk.base.Either
import ebi.ac.uk.dsl.json.JsonNull
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonNull
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
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
import uk.ac.ebi.extended.serialization.service.Properties
import uk.ac.ebi.extended.serialization.service.createExtFileList

@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionSerializerTest(
    private val tempFolder: TemporaryFolder,
) {
    private val testInstance = ExtSerializationService()

    @Test
    fun `serialize basic section`() {
        val extSection = ExtSection(type = "Study")
        val expectedJson =
            jsonObj {
                "accNo" to JsonNull
                "type" to "Study"
                "fileList" to JsonNull
                "linkList" to JsonNull
                "attributes" to jsonArray()
                "sections" to jsonArray()
                "files" to jsonArray()
                "links" to jsonArray()
                "extType" to "section"
            }.toString()

        assertThat(testInstance.serializeElement(extSection)).isEqualToIgnoringWhitespace(expectedJson)
    }

    @Test
    fun `serialize all in one`() {
        val referencedFile = tempFolder.createFile("ref-file.txt")
        val sectionFile = tempFolder.createFile("section-file.txt")
        val sectionFilesTable = tempFolder.createFile("section-file-table.txt")

        val fileNfs = tempFolder.createFile("fileNfs.txt")
        val pageTabFireFile =
            FireFile(
                fireId = "fireId",
                firePath = "firePath",
                published = false,
                filePath = "folder/fileFileName",
                relPath = "Files/folder/fileFileName",
                md5 = "fileMd5",
                size = 1,
                type = FILE,
                attributes = listOf(),
            )
        val pageTabFireDirectory =
            FireFile(
                fireId = "dirFireId",
                firePath = "firePath",
                published = false,
                filePath = "folder/dirFileName",
                relPath = "Files/folder/dirFileName",
                md5 = "dirMd5",
                size = 2,
                type = DIR,
                attributes = listOf(),
            )

        val allInOneSection =
            ExtSection(
                accNo = "SECT-001",
                type = "Study",
                fileList =
                    ExtFileList(
                        "file-list",
                        createExtFileList(
                            NfsFile(
                                "folder/ref-file.txt",
                                "Files/folder/ref-file.txt",
                                referencedFile,
                                referencedFile.absolutePath,
                                referencedFile.md5(),
                                referencedFile.size(),
                            ),
                        ),
                        pageTabFiles =
                            listOf(
                                pageTabFireFile,
                                pageTabFireDirectory,
                                NfsFile(
                                    "folder/${fileNfs.name}",
                                    "Files/folder/${fileNfs.name}",
                                    fileNfs,
                                    fileNfs.absolutePath,
                                    fileNfs.md5(),
                                    fileNfs.size(),
                                ),
                            ),
                    ),
                linkList =
                    ExtLinkList(
                        "link-list",
                        tempFolder.createFile("link-list.tsv"),
                        pageTabFiles =
                            listOf(
                                pageTabFireFile,
                                NfsFile(
                                    "folder/${fileNfs.name}",
                                    "Files/folder/${fileNfs.name}",
                                    fileNfs,
                                    fileNfs.absolutePath,
                                    fileNfs.md5(),
                                    fileNfs.size(),
                                ),
                            ),
                    ),
                attributes =
                    listOf(
                        ExtAttribute("Title", "Test Section"),
                        ExtAttribute("Description", value = null, true),
                    ),
                sections =
                    listOf(
                        Either.left(ExtSection(type = "Exp")),
                        Either.right(ExtSectionTable(listOf(ExtSection(type = "Data")))),
                    ),
                files =
                    listOf(
                        Either.left(
                            NfsFile(
                                "folder/section-file.txt",
                                "Files/folder/section-file.txt",
                                sectionFile,
                                sectionFile.absolutePath,
                                sectionFile.md5(),
                                sectionFile.size(),
                            ),
                        ),
                        Either.right(
                            ExtFileTable(
                                listOf(
                                    NfsFile(
                                        "folder/section-file-table.txt",
                                        "Files/folder/section-file-table.txt",
                                        sectionFilesTable,
                                        sectionFilesTable.absolutePath,
                                        sectionFilesTable.md5(),
                                        sectionFilesTable.size(),
                                    ),
                                ),
                            ),
                        ),
                    ),
                links =
                    listOf(
                        Either.left(ExtLink(url = "https://mylink.org")),
                        Either.right(ExtLinkTable(listOf(ExtLink(url = "https://mytable.org")))),
                    ),
            )
        val expectedJson =
            jsonObj {
                "accNo" to "SECT-001"
                "type" to "Study"
                "fileList" to
                    jsonObj {
                        "fileName" to "file-list"
                        "filesUrl" to "/submissions/extended/S-BSST1/referencedFiles/file-list"
                        "file" to allInOneSection.fileList!!.file.absolutePath
                        "pageTabFiles" to
                            jsonArray(
                                jsonObj {
                                    "fileName" to pageTabFireFile.fileName
                                    "filePath" to pageTabFireFile.filePath
                                    "relPath" to pageTabFireFile.relPath
                                    "fireId" to pageTabFireFile.fireId
                                    "firePath" to pageTabFireFile.firePath
                                    "published" to pageTabFireDirectory.published
                                    "attributes" to jsonArray()
                                    "extType" to "fireFile"
                                    "type" to "file"
                                    "md5" to pageTabFireFile.md5
                                    "size" to pageTabFireFile.size
                                },
                                jsonObj {
                                    "fileName" to pageTabFireDirectory.fileName
                                    "filePath" to pageTabFireDirectory.filePath
                                    "relPath" to pageTabFireDirectory.relPath
                                    "fireId" to pageTabFireDirectory.fireId
                                    "firePath" to pageTabFireDirectory.firePath
                                    "published" to pageTabFireDirectory.published
                                    "attributes" to jsonArray()
                                    "extType" to "fireFile"
                                    "type" to "directory"
                                    "md5" to pageTabFireDirectory.md5
                                    "size" to pageTabFireDirectory.size
                                },
                                jsonObj {
                                    "fileName" to fileNfs.name
                                    "filePath" to "folder/${fileNfs.name}"
                                    "relPath" to "Files/folder/${fileNfs.name}"
                                    "fullPath" to fileNfs.absolutePath
                                    "md5" to fileNfs.md5()
                                    "attributes" to jsonArray()
                                    "extType" to "nfsFile"
                                    "type" to "file"
                                    "size" to fileNfs.size()
                                },
                            )
                    }
                "linkList" to
                    jsonObj {
                        "fileName" to "link-list"
                        "file" to allInOneSection.linkList!!.file.absolutePath
                        "pageTabFiles" to
                            jsonArray(
                                jsonObj {
                                    "fileName" to pageTabFireFile.fileName
                                    "filePath" to pageTabFireFile.filePath
                                    "relPath" to pageTabFireFile.relPath
                                    "fireId" to pageTabFireFile.fireId
                                    "firePath" to pageTabFireFile.firePath
                                    "published" to pageTabFireDirectory.published
                                    "attributes" to jsonArray()
                                    "extType" to "fireFile"
                                    "type" to "file"
                                    "md5" to pageTabFireFile.md5
                                    "size" to pageTabFireFile.size
                                },
                                jsonObj {
                                    "fileName" to fileNfs.name
                                    "filePath" to "folder/${fileNfs.name}"
                                    "relPath" to "Files/folder/${fileNfs.name}"
                                    "fullPath" to fileNfs.absolutePath
                                    "md5" to fileNfs.md5()
                                    "attributes" to jsonArray()
                                    "extType" to "nfsFile"
                                    "type" to "file"
                                    "size" to fileNfs.size()
                                },
                            )
                    }
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "name" to "Title"
                            "value" to "Test Section"
                            "reference" to false
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        },
                        jsonObj {
                            "name" to "Description"
                            "value" to null
                            "reference" to true
                            "nameAttrs" to jsonArray()
                            "valueAttrs" to jsonArray()
                        },
                    )

                "sections" to
                    jsonArray(
                        jsonObj {
                            "accNo" to jsonNull
                            "type" to "Exp"
                            "fileList" to jsonNull
                            "linkList" to jsonNull
                            "attributes" to jsonArray()
                            "sections" to jsonArray()
                            "files" to jsonArray()
                            "links" to jsonArray()
                            "extType" to "section"
                        },
                        jsonObj {
                            "sections" to
                                jsonArray(
                                    jsonObj {
                                        "accNo" to jsonNull
                                        "type" to "Data"
                                        "fileList" to jsonNull
                                        "linkList" to jsonNull
                                        "attributes" to jsonArray()
                                        "sections" to jsonArray()
                                        "files" to jsonArray()
                                        "links" to jsonArray()
                                        "extType" to "section"
                                    },
                                )
                            "extType" to "sectionsTable"
                        },
                    )

                "files" to
                    jsonArray(
                        jsonObj {
                            "fileName" to "section-file.txt"
                            "filePath" to "folder/section-file.txt"
                            "relPath" to "Files/folder/section-file.txt"
                            "fullPath" to sectionFile.absolutePath
                            "md5" to fileNfs.md5()
                            "attributes" to jsonArray()
                            "extType" to "nfsFile"
                            "type" to "file"
                            "size" to 0
                        },
                        jsonObj {
                            "files" to
                                jsonArray(
                                    jsonObj {
                                        "fileName" to "section-file-table.txt"
                                        "filePath" to "folder/section-file-table.txt"
                                        "relPath" to "Files/folder/section-file-table.txt"
                                        "fullPath" to sectionFilesTable.absolutePath
                                        "md5" to fileNfs.md5()
                                        "attributes" to jsonArray()
                                        "extType" to "nfsFile"
                                        "type" to "file"
                                        "size" to 0
                                    },
                                )
                            "extType" to "filesTable"
                        },
                    )

                "links" to
                    jsonArray(
                        jsonObj {
                            "url" to "https://mylink.org"
                            "attributes" to jsonArray()
                            "extType" to "link"
                        },
                        jsonObj {
                            "links" to
                                jsonArray(
                                    jsonObj {
                                        "url" to "https://mytable.org"
                                        "attributes" to jsonArray()
                                        "extType" to "link"
                                    },
                                )
                            "extType" to "linksTable"
                        },
                    )

                "extType" to "section"
            }.toString()

        ExtSectionSerializer.parentAccNo = "S-BSST1"
        assertThat(
            testInstance.serializeElement(
                allInOneSection,
                Properties(includeFileListFiles = false),
            ),
        ).isEqualToIgnoringWhitespace(expectedJson)
    }
}
