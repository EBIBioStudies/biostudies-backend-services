package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ac.uk.ebi.biostd.submission.test.createTestUser
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

const val VALID_PROJECT = "BioImages"
const val INVALID_PROJECT = "BioPDFs"

@ExtendWith(MockKExtension::class)
class SubmissionProjectProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private val testInstance = SubmissionProjectProcessor()
    private lateinit var submission: ExtendedSubmission

    @BeforeEach
    fun beforeEach() {
        submission = createBasicExtendedSubmission()
        initTestProjects()
    }

    @AfterEach
    fun afterEach() {
        clearMocks(mockPersistenceContext)
    }

    @Test
    fun `submission without project`() {
        processSubmission()
        verify(exactly = 0) {
            mockPersistenceContext.getSubmission(VALID_PROJECT)
            mockPersistenceContext.getSubmission(INVALID_PROJECT)
        }
    }

    @Test
    fun `submission with null project`() {
        submission.attachTo = null

        processSubmission()
        verify(exactly = 0) {
            mockPersistenceContext.getSubmission(VALID_PROJECT)
            mockPersistenceContext.getSubmission(INVALID_PROJECT)
        }
    }

    @Test
    fun `submission with valid project`() {
        submission.attachTo = VALID_PROJECT

        processSubmission()
        verify(exactly = 1) { mockPersistenceContext.getSubmission(VALID_PROJECT) }
    }

    @Test
    fun `submission with invalid project`() {
        submission.attachTo = INVALID_PROJECT

        val exception = assertThrows<InvalidProjectException> { processSubmission() }
        assertThat(exception).hasMessage("The project BioPDFs doesn't exist")
        verify(exactly = 1) { mockPersistenceContext.getSubmission(INVALID_PROJECT) }
    }

    private fun processSubmission() = testInstance.process(submission, mockPersistenceContext)

    private fun initTestProjects() {
        every { mockPersistenceContext.getSubmission(INVALID_PROJECT) } returns null

        val project = ExtendedSubmission(VALID_PROJECT, createTestUser())
        every { mockPersistenceContext.getSubmission(VALID_PROJECT) } returns project
    }
}
