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
        assertThat(testInstance.exists(file.name)).isTrue()
    }

    @Test
    fun getFile() {
        assertThat(testInstance.getFile(file.name)).isEqualTo(file)
    }

    @Test
    fun size() {
        assertThat(testInstance.size(file.name)).isEqualTo(19L)
    }

    @Test
    fun readText() {
        assertThat(testInstance.readText(file.name)).isEqualTo("the content of file")
    }
}
