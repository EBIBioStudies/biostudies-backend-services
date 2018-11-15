package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.model.PersistenceContext
import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import arrow.core.Option
import ebi.ac.uk.model.ExtendedSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@TestInstance(PER_CLASS)
@ExtendWith(MockKExtension::class)
class TimesProcessorTest(@MockK private val mockPersistenceContext: PersistenceContext) {
    private lateinit var submission: ExtendedSubmission
    private val mockNow: OffsetDateTime = OffsetDateTime.now()
    private val testInstance = TimesProcessor()

    @BeforeEach
    fun setUp() {
        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
        every { mockPersistenceContext.getSubmission(ACC_NO) } returns Option.empty()

        submission = createBasicExtendedSubmission()
    }

    @Test
    fun `process submission without any date`() {
        assertTimeProcessing(mockNow, mockNow, mockNow)
    }

    @Test
    fun `process existing submission`() {
        val existingCreationTime = getTestTime()
        val existingSubmission = createBasicExtendedSubmission().apply {
            creationTime = existingCreationTime
        }
        every { mockPersistenceContext.getSubmission(ACC_NO) } returns Option.fromNullable(existingSubmission)

        assertTimeProcessing(existingCreationTime, mockNow, mockNow)
    }

    @Test
    fun `process submission with release date`() {
        val releaseTime = getTestTime()
        submission.releaseTime = releaseTime

        assertTimeProcessing(mockNow, releaseTime, mockNow)
    }

    private fun getTestTime() = OffsetDateTime.of(2018, 10, 10, 10, 10, 10, 10, ZoneOffset.UTC)

    private fun assertTimeProcessing(
        creationTime: OffsetDateTime,
        releaseTime: OffsetDateTime,
        modificationTime: OffsetDateTime
    ) {
        testInstance.process(submission, mockPersistenceContext)

        assertThat(submission.creationTime).isEqualTo(creationTime)
        assertThat(submission.releaseTime).isEqualTo(releaseTime)
        assertThat(submission.modificationTime).isEqualTo(modificationTime)
    }
}
