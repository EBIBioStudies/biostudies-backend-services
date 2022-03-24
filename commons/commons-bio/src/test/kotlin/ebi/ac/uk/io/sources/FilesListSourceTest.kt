package ebi.ac.uk.io.sources

import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
internal class FilesListSourceTest(temporaryFolder: TemporaryFolder) {
    private val file: File = temporaryFolder.createFile("abc.txt", "the content of file")
    private val files: List<File> = listOf(file)

    private val testInstance: FilesListSource = FilesListSource(files)

    @Test
    fun getFile() {
        val result = testInstance.getFile(file.name)
        assertThat(result).isInstanceOf(NfsBioFile::class.java)
        assertThat(result).isNotNull()

        assertThat(result!!.file).isEqualTo(file)
        assertThat(result.readContent()).isEqualTo(file.readText())
        assertThat(result.md5()).isEqualTo(file.md5())
        assertThat(result.size()).isEqualTo(file.size())
    }

    @Test
    fun `get non existing file`() {
        assertThat(testInstance.getFile("ghost.txt")).isNull()
    }
}
