package ac.uk.ebi.biostd.submission.model

import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.io.FileNotFoundException
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

const val TEST_USER_FILE = "user.txt"
const val TEST_ATTACHED_FILE = "attached.txt"
const val TEST_ATTACHED_FILE_SIZE = 456L
const val TEST_GHOST_FILE = "ghost.txt"

@ExtendWith(TemporaryFolderExtension::class)
class MixedFilesSourceTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_USER_FILE)
    private val testInputStream = testFile.inputStream()
    private val testResourceFile = ResourceFile(TEST_ATTACHED_FILE, testInputStream, TEST_ATTACHED_FILE_SIZE)
    private val testAttachedFiles = AttachedFilesSource(listOf(testResourceFile))
    private val testUserFiles = UserFilesSource(Paths.get(temporaryFolder.root.absolutePath))
    private val testInstance = MixedFilesSource(testAttachedFiles, testUserFiles)

    @Test
    fun exists() {
        assertTrue { testInstance.exists(TEST_USER_FILE) }
        assertTrue { testInstance.exists(TEST_ATTACHED_FILE) }
        assertFalse { testInstance.exists(TEST_GHOST_FILE) }
    }

    @Test
    fun `get user file input stream`() {
        assertNotNull(testInstance.getInputStream(TEST_USER_FILE))
    }

    @Test
    fun `get attached file input stream`() {
        assertThat(testInstance.getInputStream(TEST_ATTACHED_FILE)).isEqualTo(testInputStream)
    }

    @Test
    fun `get input stream of non existing file`() {
        assertThrows<FileNotFoundException> { testInstance.getInputStream(TEST_GHOST_FILE) }
    }

    @Test
    fun `get user file size`() {
        assertThat(testInstance.size(TEST_USER_FILE)).isGreaterThan(0)
    }

    @Test
    fun `get attached file size`() {
        assertThat(testInstance.size(TEST_ATTACHED_FILE)).isEqualTo(TEST_ATTACHED_FILE_SIZE)
    }

    @Test
    fun `get size of non existing file`() {
        assertThat(testInstance.size(TEST_GHOST_FILE)).isEqualTo(0)
    }

    @Test
    fun `read user file text`() {
        assertNotNull(testInstance.readText(TEST_USER_FILE))
    }

    @Test
    fun `read attached file text`() {
        assertNotNull(testInstance.readText(TEST_ATTACHED_FILE))
    }

    @Test
    fun `read text of non existing file`() {
        assertThrows<FileNotFoundException> { testInstance.readText(TEST_GHOST_FILE) }
    }
}
