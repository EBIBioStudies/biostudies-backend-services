package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.test.basicExtSubmission
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToSubmissionTest {
    private val extSubmission = basicExtSubmission.copy(
        rootPath = "/a/root/path",
        collections = listOf(ExtCollection("BioImages")),
        releaseTime = OffsetDateTime.of(2019, 9, 21, 0, 0, 0, 0, UTC),
        attributes = listOf(
            ExtAttribute("Type", "Experiment"),
            ExtAttribute("CollectionValidator", "BioImagesValidator")
        )
    )
    private val testInstance = ToSubmission(ToSection(ToFileList()))

    @Test
    fun toSimpleSubmission() {
        val submission = testInstance.toSimpleSubmission(extSubmission)
        assertThat(submission.accNo).isEqualTo("S-TEST123")
        assertSection(submission)
        assertSubmissionAttributes(submission)
    }

    private fun assertSection(submission: Submission) {
        assertThat(submission.section.type).isEqualTo("Study")
        assertThat(submission.section.attributes).isEmpty()
        assertThat(submission.section.files).isEmpty()
        assertThat(submission.section.links).isEmpty()
        assertThat(submission.section.sections).isEmpty()
    }

    private fun assertSubmissionAttributes(submission: Submission) {
        assertThat(submission.attributes).hasSize(5)
        assertThat(submission.attributes).contains(Attribute("Type", "Experiment"))
        assertThat(submission.title).isEqualTo("Test Submission")
        assertThat(submission.attachTo).isEqualTo("BioImages")
        assertThat(submission.releaseDate).isEqualTo("2019-09-21")
        assertThat(submission.rootPath).isEqualTo("/a/root/path")
    }
}
