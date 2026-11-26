package uk.ac.ebi.biostd.extended.mapping.to

import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtCollection
import uk.ac.ebi.biostd.model.Attribute
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.model.extensions.attachTo
import uk.ac.ebi.biostd.model.extensions.doi
import uk.ac.ebi.biostd.model.extensions.releaseDate
import uk.ac.ebi.biostd.model.extensions.rootPath
import uk.ac.ebi.biostd.model.extensions.title
import uk.ac.ebi.biostd.test.basicExtSubmission
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

@ExtendWith(MockKExtension::class)
class ToSubmissionMapperTest(
    @MockK val toSectionMapper: ToSectionMapper,
) {
    private val extSubmission =
        basicExtSubmission.copy(
            doi = "10.983/S-TEST123",
            rootPath = "/a/root/path",
            collections = listOf(ExtCollection("BioImages")),
            releaseTime = OffsetDateTime.of(2019, 9, 21, 0, 0, 0, 0, UTC),
            attributes =
                listOf(
                    ExtAttribute("Type", "Experiment"),
                    ExtAttribute("CollectionValidator", "BioImagesValidator"),
                ),
        )
    private val testInstance = ToSubmissionMapper(toSectionMapper)

    @Test
    fun toSimpleSubmission() =
        runTest {
            val section = Section()
            coEvery { toSectionMapper.convert(extSubmission.section, false) } returns section

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
