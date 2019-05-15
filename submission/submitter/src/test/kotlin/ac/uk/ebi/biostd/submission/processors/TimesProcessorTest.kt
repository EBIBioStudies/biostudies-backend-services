package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class TimesProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private lateinit var submission: ExtendedSubmission
    private val mockNow: OffsetDateTime = OffsetDateTime.now()
    private val testInstance = TimesProcessor()

    private val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)

    @BeforeEach
    fun setUp() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
        every { mockPersistenceContext.getSubmission(ACC_NO) } returns null

        submission = createBasicExtendedSubmission()
    }

    @Test
    fun `process submission when no existing submission`() {
        testInstance.process(submission, mockPersistenceContext)

        assertTimeProcessing(mockNow, mockNow, mockNow)
    }

    @Test
    fun `process existing submission`() {
        val existingSubmission = createBasicExtendedSubmission().apply { creationTime = testTime }
        every { mockPersistenceContext.getSubmission(ACC_NO) } returns existingSubmission

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(testTime, mockNow, mockNow)
    }

    @Test
    fun `process submission with release date`() {
        submission.releaseDate = "2018-10-10"

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(mockNow, testTime, mockNow)
    }

    @Test
    fun `process submission with null release date`() {
        submission.releaseDate = null

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(mockNow, mockNow, mockNow)
    }

    @Test
    fun `process submission with release date with invalid format`() {
        submission.releaseDate = "2018/10/10"

        val exception = assertThrows<InvalidDateFormatException> {
            testInstance.process(submission, mockPersistenceContext)
        }

        assertThat(exception.message).isEqualTo(
            "Invalid date format provided for date 2018/10/10. Expected format is YYYY-MM-DD")
    }

    private fun assertTimeProcessing(
        creationTime: OffsetDateTime,
        releaseTime: OffsetDateTime,
        modificationTime: OffsetDateTime
    ) {
        assertThat(submission.creationTime).isEqualTo(creationTime)
        assertThat(submission.releaseTime).isEqualTo(releaseTime)
        assertThat(submission.modificationTime).isEqualTo(modificationTime)
    }
}
