package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.filesystem.api.FilesService
import ac.uk.ebi.biostd.persistence.filesystem.pagetab.PageTabService
import ac.uk.ebi.biostd.persistence.filesystem.service.FileSystemService
import ebi.ac.uk.extended.model.ExtSubmission
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FileSystemServiceTest(
    @MockK private val processedSubmission: ExtSubmission,
    @MockK private val submission: ExtSubmission,
    @MockK private val finalSub: ExtSubmission,
    @MockK private val filesService: FilesService,
    @MockK private val pageTabService: PageTabService,
) {
    private val testInstance = FileSystemService(filesService, pageTabService)

    @BeforeEach
    fun beforeEach() {
        setUpServices()
        setUpSubmission()
    }

    @AfterEach
    fun afterEach() = clearAllMocks()

    @Test
    fun `persist submission`() {
        assertThat(testInstance.persistSubmissionFiles(submission)).isEqualTo(finalSub)

        verify(exactly = 1) {
            filesService.persistSubmissionFiles(submission)
            pageTabService.generatePageTab(processedSubmission)
        }
    }

    private fun setUpSubmission() {
        every { submission.accNo } returns "S-TEST123"
        every { submission.owner } returns "user@mail.org"
    }

    private fun setUpServices() {
        every { filesService.persistSubmissionFiles(submission) } returns processedSubmission
        every { pageTabService.generatePageTab(processedSubmission) } answers { finalSub }
    }
}
