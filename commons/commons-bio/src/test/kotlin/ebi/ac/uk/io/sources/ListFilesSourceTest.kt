package ebi.ac.uk.io.sources

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
internal class ListFilesSourceTest(temporaryFolder: TemporaryFolder) {

    private val file: File = temporaryFolder.createFile("abc.txt", "the content of file")
    private val files: List<File> = listOf(file)

    private val testInstance: ListFilesSource = ListFilesSource(files)

    @Test
    fun exists() {
        assertThat(testInstance.exists(file.name)).isTrue()
    }

    @Test
    fun `don't exist`() {
        assertThat(testInstance.exists("ghost.txt")).isFalse()
    }

    @Test
    fun getFile() {
        val result = testInstance.getFile(file.name)
        assertThat(result).isInstanceOf(NfsBioFile::class.java)
        assertThat(result.file).isEqualTo(file)
    }

    @Test
    fun `get non existing file`() {
        val exception = assertThrows<FileNotFoundException> { testInstance.getFile("ghost.txt") }
        assertThat(exception.message).isEqualTo("File not found: ghost.txt")
    }
}
