package ac.uk.ebi.biostd.persistence.filesystem.nfs

import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.io.ext.createDirectory
import ebi.ac.uk.io.ext.createFile
import ebi.ac.uk.paths.SubmissionFolderResolver
import ebi.ac.uk.test.basicExtSubmission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertFails

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class NfsFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val folderResolver: SubmissionFolderResolver,
) {
    @Nested
    inner class NotIncludingSecretKey {
        private val publicFolder = tempFolder.createDirectory("public")
        private val privateFolder = tempFolder.createDirectory("private")

        private val testInstance = NfsFilesService(fireClient, folderResolver)

        @BeforeEach
        fun beforeEach() {
            every { folderResolver.getPublicSubFolder(any()) } answers {
                val relPath = firstArg<String>()
                publicFolder.resolve(relPath).toPath()
            }
            every { folderResolver.getPrivateSubFolder(any(), any()) } answers {
                val relPath = secondArg<String>()
                privateFolder.resolve(relPath).toPath()
            }
        }

        @Test
        fun `persist NFS file`() =
            runTest {
                val sub = basicExtSubmission
                val file = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))

                val persisted = testInstance.persistSubmissionFile(sub, file) as NfsFile

                val path = "${privateFolder.absolutePath}/${sub.relPath}/${file.relPath}"
                assertThat(persisted.fullPath).isEqualTo(path)
                assertThat(Files.exists(Paths.get(path))).isTrue()
            }

        @Test
        fun `persist FIRE file`() =
            runTest {
                val sub = basicExtSubmission
                val downloaded = tempFolder.createFile("file.txt")
                val fireFile =
                    FireFile(
                        "fire-id",
                        "/a/file.txt",
                        true,
                        "file.txt",
                        "file.txt",
                        "md5",
                        1L,
                        FILE,
                        emptyList(),
                    )

                coEvery { fireClient.downloadByPath("/a/file.txt") } returns downloaded

                val persisted = testInstance.persistSubmissionFile(sub, fireFile) as NfsFile

                val path = "${privateFolder.absolutePath}/${sub.relPath}/${fireFile.relPath}"
                coVerify(exactly = 1) { fireClient.downloadByPath("/a/file.txt") }
                assertThat(persisted.fullPath).isEqualTo(path)
                assertThat(Files.exists(Paths.get(path))).isTrue()
            }

        @Test
        fun `delete ftp links`() =
            runTest {
                val ftpFolder = publicFolder.createDirectory("S-BSST2")
                val filesFtpFolder = ftpFolder.createDirectory("Files")
                val ftpFile = filesFtpFolder.createFile("file1.txt")
                val nfsFile = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))
                val sub = basicExtSubmission.copy(relPath = "S-BSST2")

                testInstance.deleteFtpFile(sub, nfsFile)

                assertThat(Files.exists(ftpFile.toPath())).isFalse()
            }

        @Test
        fun `delete submission file`() =
            runTest {
                val subFolder = privateFolder.createDirectory("S-BSST3")
                val filesFolder = subFolder.createDirectory("Files")
                val file = filesFolder.createFile("file1.txt")
                val nfsFile = createNfsFile("file1.txt", "Files/file1.txt", tempFolder.createFile("file1.txt"))

                val sub = basicExtSubmission.copy(relPath = "S-BSST3")
                testInstance.deleteSubmissionFile(sub, nfsFile)

                assertThat(Files.exists(file.toPath())).isFalse()
            }

        @Test
        fun `delete fire file`(
            @MockK fireFile: FireFile,
            @MockK submission: ExtSubmission,
        ) = runTest {
            val exception = assertFails { testInstance.deleteSubmissionFile(submission, fireFile) }
            assertThat(exception.message).isEqualTo("NfsFilesService should only handle NfsFile")
        }
    }
}
