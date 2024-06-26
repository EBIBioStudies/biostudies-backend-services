package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.createNfsFile
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile
import java.util.UUID
import kotlin.test.assertFails

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class FireFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val submission: ExtSubmission,
) {
    private val testInstance = FireFilesService(fireClient)

    @BeforeEach
    fun beforeEach() {
        every { submission.accNo } returns "S-BSST1"
        every { submission.version } returns 1
        every { submission.relPath } returns "001"
    }

    @Nested
    inner class WhenFireFile {
        @Test
        fun `when fire file`() =
            runTest {
                val file = fireFile(firePath = "001/Files/folder/file.txt")

                val result = testInstance.persistSubmissionFile(submission, file)

                assertThat(result).isEqualTo(file)
                verify { fireClient wasNot Called }
            }
    }

    @Nested
    inner class WhenNfsFile {
        @Test
        fun `when file is found in fire`() =
            runTest {
                val fireApiFile = fireApiFile(firePath = "001/Files/folder/file.txt")
                val file = createNfsFile("file.txt", "Files/folder/file.txt", tempFolder.createFile("file.txt", "content"))

                coEvery { fireClient.findByPath("001/Files/folder/file.txt") } returns fireApiFile

                val result = testInstance.persistSubmissionFile(submission, file)

                assertThat(result.md5).isEqualTo(file.md5)
                assertThat(result.size).isEqualTo(file.size)
                assertThat(result.fileName).isEqualTo(file.fileName)
                assertThat(result.fireId).isEqualTo(fireApiFile.fireOid)
                assertThat(result.firePath).isEqualTo("001/Files/folder/file.txt")
            }

        @Test
        fun `when file is not found in fire`() =
            runTest {
                val file = createNfsFile("file.txt", "Files/folder/file.txt", tempFolder.createFile("file.txt", "content"))
                coEvery { fireClient.findByPath("001/Files/folder/file.txt") } returns null

                val newFile = fireApiFile(firePath = null)
                val fileWithPath = fireApiFile(firePath = "001/Files/folder/file.txt")
                coEvery { fireClient.save(file.file, file.md5, file.size) } returns newFile
                coEvery { fireClient.setPath(newFile.fireOid, "001/Files/folder/file.txt") } returns fileWithPath

                val result = testInstance.persistSubmissionFile(submission, file)

                assertThat(result.fileName).isEqualTo(file.fileName)
                assertThat(result.fireId).isEqualTo(fileWithPath.fireOid)
                assertThat(result.firePath).isEqualTo("001/Files/folder/file.txt")
            }
    }

    @Nested
    inner class CleanSubmissionFiles {
        @BeforeEach
        fun beforeEach() {
            mockkStatic("uk.ac.ebi.extended.serialization.service.ExtSerializationServiceExtKt")
        }

        @Test
        fun `delete submission file`(
            @MockK submission: ExtSubmission,
        ) = runTest {
            val file = fireFile(firePath = "a file path")
            coEvery { fireClient.delete(file.fireId) } answers { nothing }

            testInstance.deleteSubmissionFile(submission, file)

            coVerify(exactly = 1) { fireClient.delete(file.fireId) }
        }

        @Test
        fun `delete nfs file`(
            @MockK nfsFile: NfsFile,
            @MockK submission: ExtSubmission,
        ) = runTest {
            every { nfsFile.filePath } returns "the-file-path/text"
            val exception = assertFails { testInstance.deleteSubmissionFile(submission, nfsFile) }

            assertThat(exception.message)
                .isEqualTo("FireFilesService should only handle FireFile, 'the-file-path/text' it is not")
        }
    }

    private fun fireFile(
        firePath: String,
        md5: String = "the md5",
    ) = FireFile(
        fireId = UUID.randomUUID().toString(),
        firePath = firePath,
        published = false,
        filePath = "folder/file.txt",
        relPath = "Files/folder/file.txt",
        md5 = md5,
        size = 123L,
        type = FILE,
        attributes = emptyList(),
    )

    private fun fireApiFile(firePath: String?) =
        FireApiFile(
            objectId = 456,
            filesystemEntry = FileSystemEntry(path = firePath, published = false),
            fireOid = UUID.randomUUID().toString(),
            objectMd5 = "the-md5",
            objectSize = 123L,
            createTime = "2022-09-21",
        )
}
