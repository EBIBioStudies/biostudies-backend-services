package ac.uk.ebi.biostd.submission.model

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

const val TEST_FILE_NAME = "test.txt"
const val TEST_FAKE_FILE_NAME = "fake.txt"

@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
class AttachedFilesSourceTest(temporaryFolder: TemporaryFolder) {
    private val testResourceFile = temporaryFolder.createFile(TEST_FILE_NAME)
    private val testInstance = AttachedFilesSource(listOf(testResourceFile))

    @Test
    fun exists() {
        assertTrue { testInstance.exists(TEST_FILE_NAME) }
        assertFalse { testInstance.exists(TEST_FAKE_FILE_NAME) }
    }

    @Test
    fun size() {
        assertThat(testInstance.size(TEST_FILE_NAME)).isEqualTo(123L)
    }

    @Test
    fun `size of non existing file`() {
        assertThrows<NoSuchElementException> { testInstance.size(TEST_FAKE_FILE_NAME) }
    }

    @Test
    fun readText() {
        assertNotNull(testInstance.readText(TEST_FILE))
    }

    @Test
    fun `read text of non existing file`() {
        assertThrows<NoSuchElementException> { testInstance.readText(TEST_FAKE_FILE_NAME) }
    }
}
