package ac.uk.ebi.biostd.submission.validators

import ac.uk.ebi.biostd.submission.exceptions.INVALID_PROJECT_ERROR_MSG
import ac.uk.ebi.biostd.submission.exceptions.InvalidProjectException
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ac.uk.ebi.biostd.submission.test.createTestUser
import arrow.core.Option
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith

const val VALID_PROJECT = "BioImages"
const val INVALID_PROJECT = "BioPDFs"

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
class ProjectValidatorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private val testInstance = ProjectValidator()
    private lateinit var submission: ExtendedSubmission

    @BeforeEach
    fun beforeEach() {
        initTestProjects()
        submission = createBasicExtendedSubmission()
    }

    @AfterEach
    fun afterEach() {
        clearMocks(mockPersistenceContext)
    }

    @Test
    fun `validate submission without project`() {
        validateSubmission()
        verify(exactly = 0) {
            mockPersistenceContext.getSubmission(VALID_PROJECT)
            mockPersistenceContext.getSubmission(INVALID_PROJECT)
        }
    }

    @Test
    fun `validate submission with null project`() {
        submission.attachTo = null

        validateSubmission()
        verify(exactly = 0) {
            mockPersistenceContext.getSubmission(VALID_PROJECT)
            mockPersistenceContext.getSubmission(INVALID_PROJECT)
        }
    }

    @Test
    fun `validate submission with valid project`() {
        submission.attachTo = VALID_PROJECT

        validateSubmission()
        verify(exactly = 1) { mockPersistenceContext.getSubmission(VALID_PROJECT) }
    }

    @Test
    fun `validate submission with invalid project`() {
        submission.attachTo = INVALID_PROJECT

        assertThatExceptionOfType(InvalidProjectException::class.java)
                .isThrownBy { validateSubmission() }
                .withMessage(INVALID_PROJECT_ERROR_MSG.format(INVALID_PROJECT))

        verify(exactly = 1) { mockPersistenceContext.getSubmission(INVALID_PROJECT) }
    }

    private fun validateSubmission() = testInstance.validate(submission, mockPersistenceContext)

    private fun initTestProjects() {
        every { mockPersistenceContext.getSubmission(INVALID_PROJECT) } returns Option.empty()
        every {
            mockPersistenceContext.getSubmission(VALID_PROJECT)
        } returns Option.fromNullable(ExtendedSubmission(VALID_PROJECT, createTestUser()))
    }
}
