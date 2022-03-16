package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class PathFilesSourceTest(
    private val temporaryFolder: TemporaryFolder
) {
    private val file = temporaryFolder.createFile("abc.txt", "the content of file")
    private val testInstance = PathFilesSource(temporaryFolder.root.toPath(), null)

    @Test
    fun exists() {
        assertThat(testInstance.exists(file.name)).isTrue
    }

    @Test
    fun getFile() {
        val result = testInstance.getFile(file.name)
        assertThat(result).isInstanceOf(NfsBioFile::class.java)
        assertThat(result.file).isEqualTo(file)
    }

    @Test
    fun `file not found`() {
        val exception = assertThrows<FileNotFoundException> { testInstance.getFile("not.txt") }
        assertThat(exception.message).isEqualTo("File not found: not.txt")
    }

    @Test
    fun `file not found with root path`() {
        val rootPathInstance = PathFilesSource(temporaryFolder.root.toPath(), "root/path")
        val exception = assertThrows<FileNotFoundException> { rootPathInstance.getFile("not.txt") }
        assertThat(exception.message).isEqualTo("File not found: root/path/not.txt")
    }
}
