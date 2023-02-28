package ebi.ac.uk.io.sources

import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.Attribute
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
internal class FilesListSourceTest(temporaryFolder: TemporaryFolder) {
    private val file: File = temporaryFolder.createFile("abc.txt")
    private val files: List<File> = listOf(file)
    private val attributes = emptyList<Attribute>()

    private val testInstance: FilesListSource = FilesListSource(files)

    @Test
    fun getFile() {
        val result = testInstance.getExtFile(file.name, attributes)
        assertThat(result).isInstanceOf(NfsFile::class.java)
        assertThat(result).isNotNull()

        val nfsFile = result as NfsFile
        assertThat(nfsFile.file).isEqualTo(file)
    }

    @Test
    fun `get non existing file`() {
        assertThat(testInstance.getExtFile("ghost.txt", attributes)).isNull()
    }
}
