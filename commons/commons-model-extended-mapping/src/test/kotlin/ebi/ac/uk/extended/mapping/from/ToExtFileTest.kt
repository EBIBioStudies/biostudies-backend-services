package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToExtFileTest(private val tempFolder: TemporaryFolder) {
    private val file = File("folder/file.txt", 55L, "file", listOf(attribute))
    private val systemFile = tempFolder.createDirectory("folder").createNewFile("file.txt")

    @Test
    fun `nfs file to ext file`() {
        val extFile = file.toExtFile(PathFilesSource(tempFolder.root.toPath())) as NfsFile

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.filePath).isEqualTo(file.path)
        assertThat(extFile.relPath).isEqualTo("Files/${file.path}")
        assertThat(extFile.fullPath).isEqualTo(systemFile.absolutePath)
        assertThat(extFile.file).isEqualTo(systemFile)
        assertAttributes(extFile)
    }

    @Test
    fun `fire file to ext file`(
        @MockK fileSource: FilesSource
    ) {
        every { fileSource.getFile("folder/file.txt") } returns FireBioFile(
            "fire-id",
            "file.txt",
            "md5",
            12,
            lazy { "content" }
        )

        val extFile = file.toExtFile(fileSource) as FireFile

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.filePath).isEqualTo(file.path)
        assertThat(extFile.relPath).isEqualTo("Files/${file.path}")
        assertThat(extFile.fireId).isEqualTo("fire-id")
        assertThat(extFile.md5).isEqualTo("md5")
        assertThat(extFile.size).isEqualTo(12)
        assertAttributes(extFile)
    }

    @Test
    fun `fire directory to ext file`(
        @MockK fileSource: FilesSource
    ) {
        every { fileSource.getFile("folder/file.txt") } returns FireDirectoryBioFile("file.txt", "md5", 12)

        val extFile = file.toExtFile(fileSource) as FireDirectory

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.filePath).isEqualTo(file.path)
        assertThat(extFile.relPath).isEqualTo("Files/${file.path}")
        assertThat(extFile.md5).isEqualTo("md5")
        assertThat(extFile.size).isEqualTo(12)
        assertAttributes(extFile)
    }

    private fun assertAttributes(extFile: ExtFile) {
        assertThat(extFile.attributes).hasSize(1)
        assertAttribute(extFile.attributes.first(), file.attributes.first())
    }
}
