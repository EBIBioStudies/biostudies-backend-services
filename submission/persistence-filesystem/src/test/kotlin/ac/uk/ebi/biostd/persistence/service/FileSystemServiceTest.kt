package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.api.FtpService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
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
    @MockK private val ftpService: FtpService,
    @MockK private val submission: ExtSubmission,
    @MockK private val filesService: FilesService,
    @MockK private val pageTabService: PageTabService
) {
    private val testInstance = FileSystemService(ftpService, filesService, pageTabService)

    @BeforeEach
    fun beforeEach() {
        setUpServices()
        setUpSubmission()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `persist submission`() {
        testInstance.persistSubmissionFiles(submission, MOVE)

        verify(exactly = 1) {
            filesService.persistSubmissionFiles(submission, MOVE)
            ftpService.processSubmissionFiles(submission)
        }
    }

    private fun setUpSubmission() {
        every { submission.accNo } returns "S-TEST123"
    }

    private fun setUpServices() {
        every { filesService.persistSubmissionFiles(submission, MOVE) } returns submission
        every { ftpService.processSubmissionFiles(submission) } answers { nothing }
    }
}
