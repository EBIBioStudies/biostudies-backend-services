package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.PathFilesSource
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue

const val TEST_USER_FILE = "user.txt"
const val TEST_ATTACHED_FILE = "attached.txt"
const val TEST_GHOST_FILE = "ghost.txt"

@ExtendWith(TemporaryFolderExtension::class)
class FilesSourceTest(temporaryFolder: TemporaryFolder) {
    private val testFile = temporaryFolder.createFile(TEST_USER_FILE)
    private val testResourceFile = temporaryFolder.createFile(TEST_ATTACHED_FILE)
    private val testInstance = ComposedFileSource(
        listOf(
            FilesListSource(listOf(testResourceFile)),
            PathFilesSource(Paths.get(temporaryFolder.root.absolutePath))
        )
    )

    @BeforeAll
    fun beforeAll() {
        FileUtils.writeStringToFile(testResourceFile, "Test content", StandardCharsets.UTF_8)
    }

    @Test
    fun exists() {
        assertTrue { testInstance.exists(TEST_USER_FILE) }
        assertTrue { testInstance.exists(TEST_ATTACHED_FILE) }
        assertFalse { testInstance.exists(TEST_GHOST_FILE) }
    }
}
