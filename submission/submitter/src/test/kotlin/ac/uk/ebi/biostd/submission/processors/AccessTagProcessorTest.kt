package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class AccessTagProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private val testInstance = AccessTagProcessor()
    private lateinit var testSubmission: ExtendedSubmission

    @BeforeEach
    fun setUp() {
        testSubmission = createBasicExtendedSubmission()
        every { mockPersistenceContext.getParentAccessTags(testSubmission) } returns listOf("Public")
    }

    @Test
    fun process() {
        testInstance.process(testSubmission, mockPersistenceContext)

        assertThat(testSubmission.accessTags).hasSize(1)
        assertThat(testSubmission.accessTags.first()).isEqualTo("Public")
    }

    @Test
    fun `private submission with public parent`() {
        testSubmission.releaseDate = "2030-12-31"
        testInstance.process(testSubmission, mockPersistenceContext)

        assertThat(testSubmission.accessTags).isEmpty()
    }
}
