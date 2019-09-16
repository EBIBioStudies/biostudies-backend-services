package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PropertiesProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private val testInstance = PropertiesProcessor()
    private lateinit var testSubmission: ExtendedSubmission

    @BeforeEach
    fun beforeEach() {
        testSubmission = createBasicExtendedSubmission().apply {
            secretKey = "a-secret-key"
            released = true
            version = 2
        }
    }

    @Test
    fun `process existing submission`() {
        every { mockPersistenceContext.isNew(ACC_NO) } returns false

        testInstance.process(testSubmission, mockPersistenceContext)

        assertThat(testSubmission.secretKey).isEqualTo("a-secret-key")
        assertThat(testSubmission.version).isEqualTo(2)
    }

    @Test
    fun `process new submission`() {
        every { mockPersistenceContext.isNew(ACC_NO) } returns true

        testInstance.process(testSubmission, mockPersistenceContext)

        assertThat(testSubmission.secretKey).isNotEmpty()
        assertThat(testSubmission.secretKey).isNotEqualTo("a-secret-key")
        assertThat(testSubmission.version).isEqualTo(1)
    }
}
