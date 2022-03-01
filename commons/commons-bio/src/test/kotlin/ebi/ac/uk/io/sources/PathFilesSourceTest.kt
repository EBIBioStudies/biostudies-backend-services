package ebi.ac.uk.io.sources

import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
internal class PathFilesSourceTest(temporaryFolder: TemporaryFolder) {

    private val file: File = temporaryFolder.createFile("abc.txt", "the content of file")
    private val testInstance = PathFilesSource(temporaryFolder.root.toPath())

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
}
