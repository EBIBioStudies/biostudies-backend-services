package ebi.ac.uk.extended.model

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionExtensionsTest(private val temporaryFolder: TemporaryFolder) {
    @Test
    fun allSections() {
        val extSection = ExtSection(
            type = "section",
            sections = listOf(
                left(ExtSection(type = "subSec1", sections = listOf(left(ExtSection(type = "subSubSec1"))))),
                right(ExtSectionTable(listOf(ExtSection(type = "subSec2"), ExtSection(type = "subSec3"))))
            )
        )

        val result = extSection.allSections

        assertThat(result.size).isEqualTo(4)
        val (sec1, sec2, sec3, sec4) = result

        assertThat(sec1)
            .isEqualTo(ExtSection(type = "subSec1", sections = listOf(left(ExtSection(type = "subSubSec1")))))
        assertThat(sec2).isEqualTo(ExtSection(type = "subSubSec1"))
        assertThat(sec3).isEqualTo(ExtSection(type = "subSec2"))
        assertThat(sec4).isEqualTo(ExtSection(type = "subSec3"))
    }

    @Test
    fun allFiles() {
        val tmpFile = temporaryFolder.createFile("file.txt")
        val nfsFile = NfsFile("filePath", "relPath", tmpFile, tmpFile.absolutePath, tmpFile.md5(), tmpFile.size())

        val fireFile = FireFile("filePath", "relPath", "fireId", "md5", 1, FILE, listOf())

        val tmpFile2 = temporaryFolder.createFile("file2.txt")
        val nfsFile2 =
            NfsFile("filePath", "relPath", tmpFile2, tmpFile2.absolutePath, tmpFile2.md5(), tmpFile2.size())

        val extSection = ExtSection(
            type = "section",
            files = listOf(left(nfsFile), left(fireFile), right(ExtFileTable(nfsFile2)))
        )

        val result = extSection.allFiles

        assertThat(result.size).isEqualTo(3)
        val (file1, file2, file3) = result
        assertThat(file1).isEqualTo(nfsFile)
        assertThat(file2).isEqualTo(fireFile)
        assertThat(file3).isEqualTo(nfsFile2)
    }

    @Test
    fun title() {
        val sectionNoTitle = ExtSection(type = "section")
        val sectionTitle = ExtSection(type = "section", attributes = listOf(ExtAttribute("Title", "Section Title")))

        assertThat(sectionNoTitle.title).isNull()
        assertThat(sectionTitle.title).isEqualTo("Section Title")
    }
}
