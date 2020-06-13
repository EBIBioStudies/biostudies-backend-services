package uk.ac.ebi.extended.serialization.deserializers

import arrow.core.Either
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.util.collections.ifLeft
import ebi.ac.uk.util.collections.ifRight
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize

@ExtendWith(TemporaryFolderExtension::class)
class EitherExtTypeDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun link() {
        val json = jsonObj {
            "url" to "http://mylink.org"
            "extType" to "link"
        }.toString()

        val link = testInstance.deserialize<Either<ExtLink, ExtLinkTable>>(json)
        assertThat(link.isLeft()).isTrue()
        link.ifLeft {
            assertThat(it.url).isEqualTo("http://mylink.org")
        }
    }

    @Test
    fun `links table`() {
        val json = jsonObj {
            "links" to jsonArray(jsonObj {
                "url" to "http://mylink.org"
                "extType" to "link"
            })
            "extType" to "linksTable"
        }.toString()

        val link = testInstance.deserialize<Either<ExtLink, ExtLinkTable>>(json)
        assertThat(link.isRight()).isTrue()
        link.ifRight {
            val links = it.links
            assertThat(links).hasSize(1)
            assertThat(links.first().url).isEqualTo("http://mylink.org")
        }
    }

    @Test
    fun file() {
        val file = tempFolder.createFile("test-file.txt")
        val json = jsonObj {
            "file" to file.absolutePath
            "fileName" to "test-file.txt"
            "extType" to "file"
        }.toString()

        val extFile = testInstance.deserialize<Either<ExtFile, ExtFileTable>>(json)
        assertThat(extFile.isLeft()).isTrue()
        extFile.ifLeft {
            assertThat(it.file).isEqualTo(file)
            assertThat(it.fileName).isEqualTo("test-file.txt")
        }
    }

    @Test
    fun `files table`() {
        val file = tempFolder.createFile("test-file-table.txt")
        val json = jsonObj {
            "files" to jsonArray(jsonObj {
                "file" to file.absolutePath
                "fileName" to "test-file-table.txt"
                "extType" to "file"
            })
            "extType" to "filesTable"
        }.toString()

        val extFilesTable = testInstance.deserialize<Either<ExtFile, ExtFileTable>>(json)
        assertThat(extFilesTable.isRight()).isTrue()
        extFilesTable.ifRight {
            val files = it.files
            assertThat(files).hasSize(1)
            assertThat(files.first().file).isEqualTo(file)
            assertThat(files.first().fileName).isEqualTo("test-file-table.txt")
        }
    }

    @Test
    fun section() {
        val json = jsonObj {
            "type" to "Study"
            "extType" to "section"
        }.toString()

        val extSection = testInstance.deserialize<Either<ExtSection, ExtSectionTable>>(json)
        assertThat(extSection.isLeft()).isTrue()
        extSection.ifLeft {
            assertThat(it.type).isEqualTo("Study")
        }
    }

    @Test
    fun `sections table`() {
        val json = jsonObj {
            "sections" to jsonArray(jsonObj {
                "type" to "Study"
                "extType" to "section"
            })
            "extType" to "sectionsTable"
        }.toString()

        val extSectionsTable = testInstance.deserialize<Either<ExtSection, ExtSectionTable>>(json)
        assertThat(extSectionsTable.isRight()).isTrue()
        extSectionsTable.ifRight {
            val sections = it.sections
            assertThat(sections).hasSize(1)
            assertThat(sections.first().type).isEqualTo("Study")
        }
    }
}
