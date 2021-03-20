package ac.uk.ebi.biostd.persistence.common.filesystem

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.MOVE
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FileSystemServiceTest(
    @MockK private val submission: ExtSubmission,
    @MockK private val filesService: FilesService,
    @MockK private val ftpLinksService: FtpFilesService
) {
    private val testInstance = FileSystemService(filesService, ftpLinksService)

    @BeforeEach
    fun beforeEach() {
        setUpServices()
        setUpSubmission()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `persist public submission`() {
        testInstance.persistSubmissionFiles(submission, MOVE)

        verify(exactly = 1) {
            filesService.persistSubmissionFiles(submission, MOVE)
            ftpLinksService.cleanFtpFolder("S-TEST/123/S-TEST123")
            ftpLinksService.createFtpFolder("S-TEST/123/S-TEST123")
        }
    }

    @Test
    fun `persist private submission`() {
        every { submission.released } returns false
        testInstance.persistSubmissionFiles(submission, MOVE)

        verify(exactly = 0) { ftpLinksService.createFtpFolder("S-TEST/123/S-TEST123") }
        verify(exactly = 1) {
            filesService.persistSubmissionFiles(submission, MOVE)
            ftpLinksService.cleanFtpFolder("S-TEST/123/S-TEST123")
        }
    }

    private fun setUpSubmission() {
        every { submission.accNo } returns "S-TEST123"
        every { submission.released } returns true
        every { submission.relPath } returns "S-TEST/123/S-TEST123"
    }

    private fun setUpServices() {
        every { filesService.persistSubmissionFiles(submission, MOVE) } returns submission
        every { ftpLinksService.cleanFtpFolder("S-TEST/123/S-TEST123") } answers { nothing }
        every { ftpLinksService.createFtpFolder("S-TEST/123/S-TEST123") } answers { nothing }
    }
}
