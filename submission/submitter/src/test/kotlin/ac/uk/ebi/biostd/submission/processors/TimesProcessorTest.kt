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
    private val mockNow: OffsetDateTime = OffsetDateTime.of(2018, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)
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

        assertThat(submission.accessTags).hasSize(1)
        assertThat(submission.accessTags.first()).isEqualTo("Public")
    }

    @Test
    fun `process submission with release date in the future`() {
        val releaseTime = OffsetDateTime.of(2020, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)
        submission.releaseDate = "2020-12-31"

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(mockNow, releaseTime, mockNow)

        assertThat(submission.accessTags).isEmpty()
    }

    @Test
    fun `process submission with null release date`() {
        submission.releaseDate = null

        testInstance.process(submission, mockPersistenceContext)
        assertTimeProcessing(mockNow, mockNow, mockNow)

        assertThat(submission.accessTags).isEmpty()
    }

    @Test
    fun `process submission with release date with invalid format`() {
        submission.releaseDate = "2018/10/10"

        val exception = assertThrows<InvalidDateFormatException> {
            testInstance.process(submission, mockPersistenceContext)
        }

        assertThat(exception.message).isEqualTo(
            "Provided date 2018/10/10 could not be parsed. Expected format is YYYY-MM-DD")
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
