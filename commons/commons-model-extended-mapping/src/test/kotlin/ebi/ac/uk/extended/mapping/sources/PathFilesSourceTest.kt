package ebi.ac.uk.extended.mapping.sources

import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.PathFilesSource
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
    fun getFile() {
        val result = testInstance.getFile(file.name)

        assertThat(result).isInstanceOf(NfsFile::class.java)
        assertThat((result as NfsFile).file).isEqualTo(file)
    }
}
