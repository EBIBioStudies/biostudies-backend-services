package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.base.Either
import uk.ac.ebi.biostd.dsl.json.jsonArray
import uk.ac.ebi.biostd.dsl.json.jsonObj
import uk.ac.ebi.biostd.io.ext.md5
import uk.ac.ebi.biostd.io.ext.size
import uk.ac.ebi.biostd.util.collections.ifLeft
import uk.ac.ebi.biostd.util.collections.ifRight
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(TemporaryFolderExtension::class)
class EitherExtTypeDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun link() {
        val json =
            jsonObj {
                "url" to "http://mylink.org"
                "extType" to "link"
            }.toString()

        val link = testInstance.readValue<Either<ExtLink, ExtLinkTable>>(json)
        assertThat(link.isLeft()).isTrue
        link.ifLeft {
            assertThat(it.url).isEqualTo("http://mylink.org")
        }
    }

    @Test
    fun `links table`() {
        val json =
            jsonObj {
                "links" to
                    jsonArray(
                        jsonObj {
                            "url" to "http://mylink.org"
                            "extType" to "link"
                        },
                    )
                "extType" to "linksTable"
            }.toString()

        val link = testInstance.readValue<Either<ExtLink, ExtLinkTable>>(json)
        assertThat(link.isRight()).isTrue
        link.ifRight {
            val links = it.links
            assertThat(links).hasSize(1)
            assertThat(links.first().url).isEqualTo("http://mylink.org")
        }
    }

    @Test
    fun file() {
        val file = tempFolder.createFile("test-file.txt")
        val json =
            jsonObj {
                "file" to file.absolutePath
                "fileName" to "test-file.txt"
                "filePath" to "folder/test-file.txt"
                "relPath" to "Files/folder/test-file.txt"
                "fullPath" to file.absolutePath
                "md5" to file.md5()
                "size" to file.size()
                "extType" to "nfsFile"
                "type" to "file"
            }.toString()

        val extFile = testInstance.readValue<Either<ExtFile, ExtFileTable>>(json)
        assertThat(extFile.isLeft()).isTrue
        extFile.ifLeft {
            it as NfsFile
            assertThat(it.file).isEqualTo(file)
            assertThat(it.fileName).isEqualTo("test-file.txt")
            assertThat(it.filePath).isEqualTo("folder/test-file.txt")
            assertThat(it.relPath).isEqualTo("Files/folder/test-file.txt")
            assertThat(it.fullPath).isEqualTo(file.absolutePath)
        }
    }

    @Test
    fun `files table`() {
        val file = tempFolder.createFile("test-file-table.txt")
        val json =
            jsonObj {
                "files" to
                    jsonArray(
                        jsonObj {
                            "file" to file.absolutePath
                            "fileName" to "test-file-table.txt"
                            "filePath" to "folder/test-file-table.txt"
                            "relPath" to "Files/folder/test-file-table.txt"
                            "fullPath" to file.absolutePath
                            "size" to file.size()
                            "md5" to file.md5()
                            "extType" to "nfsFile"
                            "type" to "file"
                        },
                    )
                "extType" to "filesTable"
            }.toString()

        val extFilesTable = testInstance.readValue<Either<ExtFile, ExtFileTable>>(json)
        assertThat(extFilesTable.isRight()).isTrue
        extFilesTable.ifRight {
            val files = it.files
            assertThat(files).hasSize(1)

            val nfsFile = files.first() as NfsFile
            assertThat(nfsFile.file).isEqualTo(file)
            assertThat(nfsFile.fileName).isEqualTo("test-file-table.txt")
            assertThat(nfsFile.filePath).isEqualTo("folder/test-file-table.txt")
            assertThat(nfsFile.relPath).isEqualTo("Files/folder/test-file-table.txt")
            assertThat(nfsFile.fullPath).isEqualTo(file.absolutePath)
        }
    }

    @Test
    fun section() {
        val json =
            jsonObj {
                "type" to "Study"
                "extType" to "section"
            }.toString()

        val extSection = testInstance.readValue<Either<ExtSection, ExtSectionTable>>(json)
        assertThat(extSection.isLeft()).isTrue
        extSection.ifLeft {
            assertThat(it.type).isEqualTo("Study")
        }
    }

    @Test
    fun `sections table`() {
        val json =
            jsonObj {
                "sections" to
                    jsonArray(
                        jsonObj {
                            "type" to "Study"
                            "extType" to "section"
                        },
                    )
                "extType" to "sectionsTable"
            }.toString()

        val extSectionsTable = testInstance.readValue<Either<ExtSection, ExtSectionTable>>(json)
        assertThat(extSectionsTable.isRight()).isTrue
        extSectionsTable.ifRight {
            val sections = it.sections
            assertThat(sections).hasSize(1)
            assertThat(sections.first().type).isEqualTo("Study")
        }
    }
}
