package ac.uk.ebi.biostd.submission.processors

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
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class TimesProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private lateinit var submission: ExtendedSubmission
    private val mockNow: OffsetDateTime = OffsetDateTime.now()
    private val testInstance = TimesProcessor()

    private val testTime = OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC)

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
        submission.releaseDate = testTime.toInstant()

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(mockNow, testTime, mockNow)
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
