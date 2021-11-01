package ebi.ac.uk.extended.model

import arrow.core.Either
import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import ebi.ac.uk.test.basicExtSubmission
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File


@ExtendWith(TemporaryFolderExtension::class)
class ExtSectionExtensionsTest(private val temporaryFolder: TemporaryFolder) {
    @Test
    fun allSections() {
        val extSection = ExtSection(
            type = "section",
            sections = listOf(
                left(ExtSection(type = "subSection1", sections = listOf(left(ExtSection(type = "subSection1A"))))),
                right(ExtSectionTable(listOf(ExtSection(type = "subSection2"), ExtSection(type = "subSection3"))))
            )
        )

        val result = extSection.allSections

        assertThat(result.size).isEqualTo(4)
        assertThat(result.first())
            .isEqualTo(ExtSection(type = "subSection1", sections = listOf(left(ExtSection(type = "subSection1A")))))
        assertThat(result.second()).isEqualTo(ExtSection(type = "subSection1A"))
        assertThat(result.third()).isEqualTo(ExtSection(type = "subSection2"))
        assertThat(result[3]).isEqualTo(ExtSection(type = "subSection3"))
    }

    @Test
    fun allReferencedFiles() {
        val nfsFile = temporaryFolder.createFile("file.txt")
        val extSection = ExtSection(
            type = "section",
            fileList = ExtFileList(
                "fileListName",
                listOf(
                    NfsFile("filePath", "relPath", nfsFile),
                    FireFile("fileName", "filePath", "relPath", "fireId", "md5", 1L, listOf()),
                    FireDirectory("fileName", "filePath", "relPath", "md5", 1L, listOf())
                )
            )
        )

        val result = extSection.allReferencedFiles

        assertThat(result.size).isEqualTo(3)
        assertThat(result.first()).isEqualTo(extSection.fileList?.files?.first())
        assertThat(result.second()).isEqualTo(extSection.fileList?.files?.second())
        assertThat(result.third()).isEqualTo(extSection.fileList?.files?.third())
    }

    @Test
    fun allFiles() {
        val nfsFile1 = temporaryFolder.createFile("file1.txt")
        val nfsFile2 = temporaryFolder.createFile("file2.txt")
        val extSection = ExtSection(
            type = "section",
            files = listOf(
                left(NfsFile("filePath", "relPath", nfsFile1)),
                left(FireFile("fileName", "filePath", "relPath", "fireId", "md5", 1, listOf())),
                right(ExtFileTable(NfsFile("filePath", "relPath", nfsFile2)))
            )
        )

        val result = extSection.allFiles

        assertThat(result.size).isEqualTo(3)
        assertThat(result.first()).isEqualTo(NfsFile("filePath", "relPath", nfsFile1))
        assertThat(result.second()).isEqualTo(FireFile("fileName", "filePath", "relPath", "fireId", "md5", 1, listOf()))
        assertThat(result.third()).isEqualTo(NfsFile("filePath", "relPath", nfsFile2))
    }
}
