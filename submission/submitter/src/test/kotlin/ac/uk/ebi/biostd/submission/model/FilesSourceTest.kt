package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.io.sources.ComposedFileSource
import ebi.ac.uk.io.sources.FilesListSource
import ebi.ac.uk.io.sources.PathFilesSource
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Paths
import kotlin.test.assertFalse
import kotlin.test.assertTrue

const val TEST_USER_FILE = "user.txt"
const val TEST_ATTACHED_FILE = "attached.txt"
const val TEST_GHOST_FILE = "ghost.txt"

@ExtendWith(TemporaryFolderExtension::class)
class FilesSourceTest(
    private val temporaryFolder: TemporaryFolder
) {
    private val testResourceFile = temporaryFolder.createFile(TEST_ATTACHED_FILE, "Test content")
    private val testInstance = ComposedFileSource(
        listOf(
            FilesListSource(listOf(testResourceFile), null),
            PathFilesSource(Paths.get(temporaryFolder.root.absolutePath), null)
        ),
        null
    )

    @BeforeEach
    fun beforeEach() {
        temporaryFolder.createFile(TEST_USER_FILE)
    }

    @Test
    fun exists() {
        assertTrue { testInstance.exists(TEST_USER_FILE) }
        assertTrue { testInstance.exists(TEST_ATTACHED_FILE) }
        assertFalse { testInstance.exists(TEST_GHOST_FILE) }
    }
}
