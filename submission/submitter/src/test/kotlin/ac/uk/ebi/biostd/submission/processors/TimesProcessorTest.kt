package ac.uk.ebi.biostd.submission.processors

import ac.uk.ebi.biostd.submission.exceptions.InvalidDateFormatException
import ac.uk.ebi.biostd.submission.test.ACC_NO
import ac.uk.ebi.biostd.submission.test.createBasicExtendedSubmission
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.constants.SubFields.PUBLIC_ACCESS_TAG
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.persistence.PersistenceContext
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset

@ExtendWith(MockKExtension::class)
class TimesProcessorTest(@MockK private val mockContext: PersistenceContext) {

    private val testTime = OffsetDateTime.of(2018, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
    private lateinit var submission: ExtendedSubmission
    private val mockNow = OffsetDateTime.of(2018, 12, 31, 0, 0, 0, 0, ZoneOffset.UTC)

    private val testInstance = TimesProcessor()

    @BeforeEach
    fun setUp() {
        submission = createBasicExtendedSubmission()

        mockkStatic(OffsetDateTime::class)
        every { OffsetDateTime.now() } returns mockNow
        every { mockContext.hasParent(submission) } returns false
        every { mockContext.getSubmission(ACC_NO) } returns null
    }

    @Nested
    inner class ModificationTime {

        @Test
        fun `calculate modification time`() {
            testInstance.process(submission, mockContext)

            assertThat(submission.modificationTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class CreationTime {

        @Test
        fun `when exists`() {
            val existingSubmission = createBasicExtendedSubmission().apply { creationTime = testTime }
            every { mockContext.getSubmission(ACC_NO) } returns existingSubmission

            testInstance.process(submission, mockContext)

            assertThat(submission.creationTime).isEqualTo(testTime)
        }

        @Test
        fun `when is new`() {
            testInstance.process(submission, mockContext)

            assertThat(submission.creationTime).isEqualTo(mockNow)
        }
    }

    @Nested
    inner class ReleaseTime {

        @Nested
        inner class WhenNoParent {

            @Test
            fun `when release date with invalid format`() {
                submission.releaseDate = "2018/10/10"

                val exception = assertThrows<InvalidDateFormatException> { testInstance.process(submission, mockContext) }

                assertThat(exception.message).isEqualTo(
                    "Provided date 2018/10/10 could not be parsed. Expected format is YYYY-MM-DD")
            }

            @Test
            fun `when release date with valid format`() {
                val releaseTime = OffsetDateTime.of(2019, 10, 10, 0, 0, 0, 0, ZoneOffset.UTC)
                submission.releaseDate = "2019-10-10T09:27:04.000Z"

                testInstance.process(submission, mockContext)

                assertThat(submission.releaseTime).isEqualTo(releaseTime)
                assertThat(submission.released).isFalse()
                assertThat(submission.accessTags).isEmpty()
            }

            @Test
            fun `when no release date now is used`() {
                testInstance.process(submission, mockContext)

                assertThat(submission.releaseTime).isEqualTo(mockNow)
                assertThat(submission.released).isTrue()
                assertThat(submission.accessTags).contains(PUBLIC_ACCESS_TAG.value)
            }
        }

        @Test
        fun `when parent project not released`() {
            every { mockContext.hasParent(submission) } returns true
            every { mockContext.getParentReleaseTime(submission) } returns null

            testInstance.process(submission, mockContext)

            assertThat(submission.releaseTime).isNull()
            assertThat(submission.released).isFalse()
            assertThat(submission.accessTags).isEmpty()
        }

        @Test
        fun `when no relase date`() {
            every { mockContext.hasParent(submission) } returns true
            every { mockContext.getParentReleaseTime(submission) } returns null

            testInstance.process(submission, mockContext)

            assertThat(submission.releaseTime).isNull()
            assertThat(submission.released).isFalse()
            assertThat(submission.accessTags).isEmpty()
        }
    }
}
