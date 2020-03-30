package ebi.ac.uk.extended.model

import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExtSubmissionExtensionsTest {
    private val testTime = OffsetDateTime.of(2018, 9, 21, 0, 0, 0, 0, ZoneOffset.UTC)

    @Test
    fun `release date`() {
        val submission = ExtSubmission(
            "S-TEST123",
            0,
            "Test Submission",
            SubmissionMethod.PAGE_TAB,
            "a/rel/path",
            "a/root/path",
            false,
            "a-secret-key",
            ProcessingStatus.PROCESSED,
            testTime,
            testTime,
            testTime,
            emptyList(),
            emptyList(),
            emptyList(),
            mockk())

        assertThat(submission.releaseDate).isEqualTo("2018-09-21")
    }

    @Test
    fun `no release date`() {
        val submission = ExtSubmission(
            "S-TEST123",
            0,
            "Test Submission",
            SubmissionMethod.PAGE_TAB,
            "a/rel/path",
            "a/root/path",
            false,
            "a-secret-key",
            ProcessingStatus.PROCESSED,
            null,
            testTime,
            testTime,
            emptyList(),
            emptyList(),
            emptyList(),
            mockk())

        assertThat(submission.releaseDate).isEmpty()
    }
}
