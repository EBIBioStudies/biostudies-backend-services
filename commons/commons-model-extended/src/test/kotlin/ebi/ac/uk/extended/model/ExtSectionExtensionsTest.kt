package ebi.ac.uk.extended.model

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
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
    fun allReferencedFiles() {
        val nfsFile = NfsFile("filePath", "relPath", temporaryFolder.createFile("file.txt"))
        val fireFile = FireFile("fileName", "filePath", "relPath", "fireId", "md5", 1L, listOf())
        val fireDirectory = FireDirectory("fileName", "filePath", "relPath", "md5", 1L, listOf())

        val extSection = ExtSection(
            type = "section",
            fileList = ExtFileList("fileListName", listOf(nfsFile, fireFile, fireDirectory))
        )

        val result = extSection.allReferencedFiles

        assertThat(result.size).isEqualTo(3)
        val (file1, file2, file3) = result
        assertThat(file1).isEqualTo(nfsFile)
        assertThat(file2).isEqualTo(fireFile)
        assertThat(file3).isEqualTo(fireDirectory)
    }

    @Test
    fun allFiles() {
        val nfsFile = NfsFile("filePath", "relPath", temporaryFolder.createFile("file1.txt"))
        val fireFile = FireFile("fileName", "filePath", "relPath", "fireId", "md5", 1, listOf())
        val nfsFile1 = NfsFile("filePath", "relPath", temporaryFolder.createFile("file2.txt"))
        val extSection = ExtSection(
            type = "section",
            files = listOf(left(nfsFile), left(fireFile), right(ExtFileTable(nfsFile1)))
        )

        val result = extSection.allFiles

        assertThat(result.size).isEqualTo(3)
        val (file1, file2, file3) = result
        assertThat(file1).isEqualTo(nfsFile)
        assertThat(file2).isEqualTo(fireFile)
        assertThat(file3).isEqualTo(nfsFile1)
    }
}
