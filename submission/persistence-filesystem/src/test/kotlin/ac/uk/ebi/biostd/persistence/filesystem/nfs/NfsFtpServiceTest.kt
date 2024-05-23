package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode.HARD_LINKS
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import kotlin.io.path.createDirectories

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(TemporaryFolderExtension::class, MockKExtension::class)
internal class NfsFtpServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val subInfo: ExtSubmissionInfo,
    @MockK private val folderResolver: SubmissionFolderResolver,
) {
    private val publicFolder = tempFolder.createDirectory("public")
    private val privateFolder = tempFolder.createDirectory("private")

    @BeforeEach
    fun beforeEach() {
        tempFolder.clean()
        privateFolder.resolve(REL_PATH).toPath().createDirectories()

        every { folderResolver.getPublicSubFolder(REL_PATH) } answers {
            val relPath = firstArg<String>()
            publicFolder.resolve(relPath).toPath()
        }
        every { folderResolver.getPrivateSubFolder(any(), any()) } answers {
            val relPath = secondArg<String>()
            privateFolder.resolve(relPath).toPath()
        }
    }

    @Nested
    inner class HardLinksMode {
        @Test
        fun `release submission file when hardlink`() {
            val testInstance = NfsFtpService(HARD_LINKS, folderResolver)

            runTest {
                every { subInfo.relPath } returns REL_PATH
                every { subInfo.secretKey } returns SECRET_KEY

                val subFolder = folderResolver.getPrivateSubFolder(SECRET_KEY, REL_PATH)
                val filesDir =
                    subFolder.resolve("Files").createDirectories()
                        .apply { createDirectories() }
                        .toFile()
                val file = filesDir.createNewFile("file.txt", "file content")

                val nfsFile =
                    NfsFile(
                        filePath = "file.txt",
                        relPath = "Files/file.txt",
                        file = file,
                        fullPath = file.absolutePath,
                        md5 = file.md5(),
                        size = file.size(),
                    )

                testInstance.releaseSubmissionFile(subInfo, nfsFile)

                val result = publicFolder.resolve("$REL_PATH/Files/file.txt")
                assertThat(result).hasSameTextualContentAs(file)
                assertThat(Files.getAttribute(file.toPath(), FILE_KEY_AATR))
                    .isEqualTo(Files.getAttribute(result.toPath(), FILE_KEY_AATR))
            }
        }

        @Test
        fun `release submission file when move`() {
            val testInstance = NfsFtpService(NfsReleaseMode.MOVE, folderResolver)

            runTest {
                every { subInfo.relPath } returns REL_PATH
                every { subInfo.secretKey } returns SECRET_KEY

                val subFolder = folderResolver.getPrivateSubFolder(SECRET_KEY, REL_PATH)
                val filesDir =
                    subFolder.resolve("Files").createDirectories()
                        .apply { createDirectories() }
                        .toFile()
                val file = filesDir.createNewFile("file.txt", "file content")

                val nfsFile =
                    NfsFile(
                        filePath = "file.txt",
                        relPath = "Files/file.txt",
                        file = file,
                        fullPath = file.absolutePath,
                        md5 = file.md5(),
                        size = file.size(),
                    )

                testInstance.releaseSubmissionFile(subInfo, nfsFile)

                val result = publicFolder.resolve("$REL_PATH/Files/file.txt")
                assertThat(result).content().isEqualTo("file content")
                assertThat(file).doesNotExist()
            }
        }
    }

    companion object {
        private const val REL_PATH = "My/Path/To/Submission"
        private const val SECRET_KEY = "secret-key"
        private const val FILE_KEY_AATR = "basic:fileKey"
    }
}
