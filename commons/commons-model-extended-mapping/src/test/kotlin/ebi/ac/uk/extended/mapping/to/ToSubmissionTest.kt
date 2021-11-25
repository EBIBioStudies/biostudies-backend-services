package ebi.ac.uk.extended.mapping.to

import DefaultSubmission.Companion.defaultSubmission
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToSubmissionTest {
    private val extSubmission = defaultSubmission(
        collections = listOf(ExtCollection("BioImages"), ExtCollection("Public")),
        attributes = listOf(
            ExtAttribute("Type", "Experiment"),
            ExtAttribute("CollectionValidator", "BioImagesValidator")
        )
    )

    @Test
    fun toSimpleSubmission() {
        val submission = extSubmission.toSimpleSubmission()
        assertThat(submission.accNo).isEqualTo(DefaultSubmission.ACC_NO)
        assertSection(submission)
        assertSubmissionAttributes(submission)
    }

    private fun assertSection(submission: Submission) {
        assertThat(submission.section.type).isEqualTo(DefaultSubmission.SECTION.type)
        assertThat(submission.section.attributes).isEmpty()
        assertThat(submission.section.files).isEmpty()
        assertThat(submission.section.links).isEmpty()
        assertThat(submission.section.sections).isEmpty()
    }

    private fun assertSubmissionAttributes(submission: Submission) {
        assertThat(submission.attributes).hasSize(5)
        assertThat(submission.attributes).contains(Attribute("Type", "Experiment"))
        assertThat(submission.title).isEqualTo(DefaultSubmission.TITLE)
        assertThat(submission.attachTo).isEqualTo("BioImages")
        assertThat(submission.releaseDate).isEqualTo(DefaultSubmission.RELEASE_TIME.toLocalDate().toString())
        assertThat(submission.rootPath).isEqualTo(DefaultSubmission.ROOT_PATH)
    }
}
