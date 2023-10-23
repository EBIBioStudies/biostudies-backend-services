package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.attachTo
import ebi.ac.uk.model.extensions.doi
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.test.basicExtSubmission
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class ToSubmissionMapperTest(
    @MockK val toSectionMapper: ToSectionMapper,
) {
    private val extSubmission = basicExtSubmission.copy(
        doi = "10.983/S-TEST123",
        rootPath = "/a/root/path",
        collections = listOf(ExtCollection("BioImages")),
        releaseTime = OffsetDateTime.of(2019, 9, 21, 0, 0, 0, 0, UTC),
        attributes = listOf(
            ExtAttribute("Type", "Experiment"),
            ExtAttribute("CollectionValidator", "BioImagesValidator")
        )
    )
    private val testInstance = ToSubmissionMapper(toSectionMapper)

    @Test
    fun toSimpleSubmission() {
        val section = Section()
        every { toSectionMapper.convert(extSubmission.section) } returns section

        val submission = testInstance.toSimpleSubmission(extSubmission)

        assertThat(submission.accNo).isEqualTo("S-TEST123")
        assertThat(submission.section).isEqualTo(section)
        assertSubmissionAttributes(submission)
    }

    private fun assertSubmissionAttributes(submission: Submission) {
        assertThat(submission.attributes).hasSize(6)
        assertThat(submission.attributes).contains(Attribute("Type", "Experiment"))
        assertThat(submission.title).isEqualTo("Test Submission")
        assertThat(submission.doi).isEqualTo("10.983/S-TEST123")
        assertThat(submission.attachTo).isEqualTo("BioImages")
        assertThat(submission.releaseDate).isEqualTo("2019-09-21")
        assertThat(submission.rootPath).isEqualTo("/a/root/path")
    }
}
