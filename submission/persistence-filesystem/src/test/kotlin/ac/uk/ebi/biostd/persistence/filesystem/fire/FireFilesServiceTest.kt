package ac.uk.ebi.biostd.persistence.filesystem.fire

import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FireFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileSystemEntry
import uk.ac.ebi.fire.client.model.FireApiFile

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class FireFilesServiceTest(
    private val tempFolder: TemporaryFolder,
    @MockK private val fireClient: FireClient,
    @MockK private val submission: ExtSubmission,
    @MockK private val serializationService: ExtSerializationService,
) {
    private val fireTempDirPath = tempFolder.createDirectory("fire-temp")
    private val testInstance = FireFilesService(fireClient, fireTempDirPath, serializationService)

    @BeforeEach
    fun beforeEach() {
        every { submission.accNo } returns "S-BSST1"
        every { submission.version } returns 1
        every { submission.relPath } returns "S-BSST/001/S-BSST1"
    }

    @Test
    fun `persist fire file found by md5 and path`() {
        val expectedPath = "/S-BSST/001/S-BSST1/Files/folder/file.txt"
        val file = fireFile(expectedPath)
        val fireApiFile = fireApiFile(expectedPath)

        every { fireClient.findByMd5("the-md5") } returns listOf(fireApiFile)

        val persisted = testInstance.persistSubmissionFile(submission, file)

        assertThat(persisted).isEqualToComparingFieldByField(file)
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
        }
        verify(exactly = 0) {
            fireClient.downloadByFireId(any(), any())
            fireClient.setPath(any(), any())
            fireClient.save(any(), any(), any())
        }
    }

    @Test
    fun `persist fire file found by md5 but no path is set`() {
        val file = fireFile(null)
        val fireApiFile = fireApiFile(null)

        every { fireClient.findByMd5("the-md5") } returns listOf(fireApiFile)
        every { fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt") } answers { nothing }

        val persisted = testInstance.persistSubmissionFile(
            submission,
            file
        )

        assertThat(persisted).isEqualToComparingFieldByField(file.copy(firePath = "/S-BSST/001/S-BSST1/Files/folder/file.txt"))
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
            fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt")
        }
        verify(exactly = 0) {
            fireClient.downloadByFireId("the-fire-id", "file.txt")
            fireClient.save(any(), "the-md5", 123L)
        }
    }

    @Test
    fun `persist fire file found not found`() {
        val file = fireFile(null)
        val fireApiFile = fireApiFile(null)
        val content = tempFolder.createFile("file.txt")

        every { fireClient.findByMd5("the-md5") } returns listOf()
        every { fireClient.save(any(), "the-md5", 123L) } returns fireApiFile
        every { fireClient.downloadByFireId("the-fire-id", "file.txt") } returns content
        every { fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt") } answers { nothing }

        val persisted = testInstance.persistSubmissionFile(submission, file)

        assertThat(persisted)
            .isEqualToComparingFieldByField(file.copy(firePath = "/S-BSST/001/S-BSST1/Files/folder/file.txt"))
        verify(exactly = 1) {
            fireClient.findByMd5("the-md5")
            fireClient.save(any(), "the-md5", 123L)
            fireClient.downloadByFireId("the-fire-id", "file.txt")
            fireClient.setPath("the-fire-id", "/S-BSST/001/S-BSST1/Files/folder/file.txt")
        }
    }

    private fun fireFile(firePath: String?) = FireFile(
        fireId = "the-fire-id",
        firePath = firePath,
        filePath = "folder/file.txt",
        relPath = "Files/folder/file.txt",
        md5 = "the-md5",
        size = 123L,
        type = FILE,
        attributes = emptyList(),
    )

    private fun fireApiFile(firePath: String?) = FireApiFile(
        objectId = 456,
        filesystemEntry = FileSystemEntry(path = firePath, published = false),
        fireOid = "the-fire-id",
        objectMd5 = "the-md5",
        objectSize = 123L,
        createTime = "2022-09-21"
    )
}
