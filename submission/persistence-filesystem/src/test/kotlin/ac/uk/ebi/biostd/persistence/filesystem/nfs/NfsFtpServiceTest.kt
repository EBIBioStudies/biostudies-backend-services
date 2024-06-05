package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode.HARD_LINKS
import ac.uk.ebi.biostd.persistence.filesystem.api.NfsReleaseMode.MOVE
import ebi.ac.uk.extended.model.ExtSubmissionInfo
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.io.ext.createNewFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.paths.FILES_PATH
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.clean
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import kotlin.io.path.createDirectories

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

        every { subInfo.relPath } returns REL_PATH
        every { subInfo.secretKey } returns SECRET_KEY
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
        private val testInstance = NfsFtpService(HARD_LINKS, folderResolver)

        @Test
        fun `release submission file`() =
            runTest {
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
                assertThat(Files.getAttribute(file.toPath(), FILE_KEY_ATTR))
                    .isEqualTo(Files.getAttribute(result.toPath(), FILE_KEY_ATTR))
            }

        @Test
        fun `suppress submission file`() =
            runTest {
                val publicFolder = folderResolver.getPublicSubFolder(REL_PATH).toFile()
                val publicFilesFolder = publicFolder.createDirectory(FILES_PATH)
                val publicFile = publicFilesFolder.createFile("test.txt")

                val nfsFile =
                    NfsFile(
                        filePath = "test.txt",
                        relPath = "Files/test.txt",
                        file = publicFile,
                        fullPath = publicFile.absolutePath,
                        md5 = publicFile.md5(),
                        size = publicFile.size(),
                    )

                testInstance.suppressSubmissionFile(subInfo, nfsFile)

                assertThat(publicFile).doesNotExist()
            }
    }

    @Nested
    inner class MoveMode {
        private val testInstance = NfsFtpService(MOVE, folderResolver)

        @Test
        fun `release submission file`() =
            runTest {
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

        @Test
        fun `suppress submission file`() =
            runTest {
                val subFolder = folderResolver.getPublicSubFolder(REL_PATH).toFile()
                val filesPath = subFolder.createDirectory(FILES_PATH)
                val file = filesPath.createFile("move-test.txt", "move content")
                val nfsFile =
                    NfsFile(
                        filePath = "move-test.txt",
                        relPath = "Files/move-test.txt",
                        file = file,
                        fullPath = file.absolutePath,
                        md5 = file.md5(),
                        size = file.size(),
                    )

                val privateFolder = folderResolver.getPrivateSubFolder(SECRET_KEY, REL_PATH).resolve(FILES_PATH)
                val suppressedPath = privateFolder.resolve("move-test.txt")
                val suppressed = testInstance.suppressSubmissionFile(subInfo, nfsFile) as NfsFile
                assertThat(suppressedPath).exists()
                assertThat(suppressed.fullPath).isEqualTo(suppressedPath.toString())
                assertThat(suppressed.file).isEqualTo(suppressedPath.toFile())
                assertThat(filesPath.resolve("move-test.txt")).doesNotExist()
            }
    }

    companion object {
        private const val REL_PATH = "My/Path/To/Submission"
        private const val SECRET_KEY = "secret-key"
        private const val FILE_KEY_ATTR = "basic:fileKey"
    }
}
