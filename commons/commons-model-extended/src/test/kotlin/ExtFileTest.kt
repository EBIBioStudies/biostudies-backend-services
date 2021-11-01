import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileTest(private val temporaryFolder: TemporaryFolder) {
    @Test
    fun `extFile with file inside folder`() {
        val file = temporaryFolder.createDirectory("Files").createDirectory("my-folder").createNewFile("file.txt")
        val extFile = NfsFile("/my-folder/file.txt", "Files/my-folder/file.txt", file, listOf())

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
    }

    @Test
    fun `extFile with simple file`() {
        val file = temporaryFolder.createDirectory("Files").createNewFile("file.txt")
        val extFile = NfsFile("file.txt", "Files/file.txt", file, listOf())

        assertThat(extFile.fileName).isEqualTo("file.txt")
        assertThat(extFile.md5).isEqualTo(file.md5())
        assertThat(extFile.size).isEqualTo(file.size())
    }
}
