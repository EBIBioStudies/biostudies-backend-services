package ac.uk.ebi.biostd.submission.model

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileNotFoundException
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

const val TEST_FILE = "test.txt"
const val FAKE_FILE = "fake.txt"

@ExtendWith(TemporaryFolderExtension::class)
class PathFilesSourceTest(private val temporaryFolder: TemporaryFolder) {
    private val testInstance = PathFilesSource(Paths.get(temporaryFolder.root.absolutePath))

    @BeforeAll
    fun setUp() {
        temporaryFolder.createFile(TEST_FILE)
    }

    @Test
    fun exists() {
        assertTrue { testInstance.exists(TEST_FILE) }
        assertFalse { testInstance.exists(FAKE_FILE) }
    }

    @Test
    fun getInputStream() {
        assertNotNull(testInstance.getInputStream(TEST_FILE))
    }

    @Test
    fun `get input stream of non existing file`() {
        assertThrows<FileNotFoundException> { testInstance.getInputStream(FAKE_FILE) }
    }

    @Test
    fun size() {
        assertThat(testInstance.size(TEST_FILE)).isGreaterThan(0)
    }

    @Test
    fun `size of non existing file`() {
        assertThat(testInstance.size(FAKE_FILE)).isEqualTo(0)
    }

    @Test
    fun readText() {
        assertNotNull(testInstance.readText(TEST_FILE))
    }

    @Test
    fun `read text of non existing file`() {
        assertThrows<FileNotFoundException> { testInstance.readText(FAKE_FILE) }
    }
}
