package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class FilesListSourceTest(temporaryFolder: TemporaryFolder) {
    private val file = temporaryFolder.createFile("abc.txt", "the content of file")
    private val files = listOf(file)

    private val testInstance: FilesListSource = FilesListSource(files, null)

    @Test
    fun exists() {
        assertThat(testInstance.exists(file.name)).isTrue
    }

    @Test
    fun `don't exist`() {
        assertThat(testInstance.exists("ghost.txt")).isFalse
    }

    @Test
    fun getFile() {
        val result = testInstance.getFile(file.name)
        assertThat(result).isInstanceOf(NfsBioFile::class.java)
        assertThat(result.file).isEqualTo(file)
        assertThat(result.readContent()).isEqualTo(file.readText())
        assertThat(result.md5()).isEqualTo(file.md5())
        assertThat(result.size()).isEqualTo(file.size())
    }

    @Test
    fun `get non existing file`() {
        val exception = assertThrows<FileNotFoundException> { testInstance.getFile("ghost.txt") }
        assertThat(exception.message).isEqualTo("File not found: ghost.txt")
    }

    @Test
    fun `get non existing file with root path`() {
        val rootPathInstance = FilesListSource(files, "root/path")
        val exception = assertThrows<FileNotFoundException> { rootPathInstance.getFile("ghost.txt") }
        assertThat(exception.message).isEqualTo("File not found: root/path/ghost.txt")
    }
}
