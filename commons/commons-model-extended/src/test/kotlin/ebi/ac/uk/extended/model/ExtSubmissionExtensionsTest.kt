package ebi.ac.uk.extended.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.time.ZoneOffset

class ExtSubmissionExtensionsTest {
    @Test
    fun computedTitle() {
        val submissionTitle = testSubmission(subTitle = "submission title", secTitle = null)
        val submissionNoTitleSecTitle = testSubmission(subTitle = null, secTitle = "section title")
        val submissionNoTitleNoSecTitle = testSubmission(subTitle = null, secTitle = null)

        assertThat(submissionTitle.computedTitle).isEqualTo("submission title")
        assertThat(submissionNoTitleSecTitle.computedTitle).isEqualTo("section title")
        assertThat(submissionNoTitleNoSecTitle.computedTitle).isNull()
    }

    private fun testSubmission(subTitle: String? = null, secTitle: String? = null): ExtSubmission = ExtSubmission(
        accNo = "S-TEST1",
        version = 1,
        schemaVersion = "1.0",
        owner = "owner@mail.org",
        storageMode = StorageMode.FIRE,
        submitter = "submitter@mail.org",
        title = subTitle,
        method = ExtSubmissionMethod.PAGE_TAB,
        relPath = "/a/rel/path",
        rootPath = null,
        releaseTime = null,
        released = true,
        secretKey = "a-secret-key",
        status = ExtProcessingStatus.PROCESSED,
        modificationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        creationTime = OffsetDateTime.of(2020, 9, 21, 10, 30, 34, 15, ZoneOffset.UTC),
        attributes = listOf(),
        section = ExtSection(
            type = "Study",
            attributes = secTitle?.let { listOf(ExtAttribute("Title", it)) } ?: listOf()
        )
    )
}
