package uk.ac.ebi.io.sources

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.model.constants.FileFields.FILE_TYPE
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File

@ExtendWith(TemporaryFolderExtension::class)
internal class PathSourceTest(
    temporaryFolder: TemporaryFolder,
) {
    private val attributes = emptyList<ExtAttribute>()
    private val file: File = temporaryFolder.createFile("abc.txt", "the content of file")

    private val testInstance = PathSource("Example description", temporaryFolder.root.toPath())

    @Test
    fun description() {
        assertThat(testInstance.description).isEqualTo("Example description")
    }

    @Test
    fun getFile() =
        runTest {
            val result = testInstance.getExtFile(file.name, FILE_TYPE.value, attributes)

            assertThat(result).isInstanceOf(NfsFile::class.java)
            assertThat((result as NfsFile).file).isEqualTo(file)
        }
}
