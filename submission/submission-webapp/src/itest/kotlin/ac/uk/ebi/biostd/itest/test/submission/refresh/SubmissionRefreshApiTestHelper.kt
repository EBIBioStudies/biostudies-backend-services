package ac.uk.ebi.biostd.itest.test.submission.refresh

import arrow.core.Either
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifLeft
import org.assertj.core.api.Assertions.assertThat
import java.time.OffsetDateTime

object SubmissionRefreshApiTestHelper {
    private const val ROOT_PATH = "test-RootPath"
    private const val RELEASE_DATE_STRING = "2017-07-04"

    internal const val ACC_NO = "SimpleAcc1"
    internal const val SUBTITLE = "Simple Submission"
    internal const val ATTR_NAME = "custom-attribute"
    internal const val ATTR_VALUE = "custom-attribute-value"
    internal const val NEW_SUBTITLE = "Simple Submission"
    internal const val NEW_ATTR_VALUE = "custom-attribute-new-value"
    internal const val TEST_FILE_NAME = "refresh-file.txt"

    internal val testSubmission = submission(ACC_NO) {
        title = SUBTITLE
        releaseDate = RELEASE_DATE_STRING
        rootPath = ROOT_PATH
        attribute(ATTR_NAME, ATTR_VALUE)

        section("Study") {
            file("refresh-file.txt") {
                attribute("type", "regular")
            }

            file("refresh-file.txt") {
                attribute("type", "duplicated")
            }
        }
    }

    internal fun assertExtSubmission(
        extSubmission: ExtSubmission,
        title: String,
        releaseTime: OffsetDateTime,
        attributes: List<Pair<String, String>>
    ) {
        assertThat(extSubmission.title).isEqualTo(title)
        assertThat(extSubmission.releaseTime).isEqualTo(releaseTime)
        assertThat(extSubmission.rootPath).isEqualTo(ROOT_PATH)
        assertThat(extSubmission.attributes.map { it.name to it.value }).containsExactlyElementsOf(attributes)

        assertThat(extSubmission.section.type).isEqualTo("Study")
        assertThat(extSubmission.section.files).hasSize(2)

        assertFile(extSubmission.section.files.first(), "regular")
        assertFile(extSubmission.section.files.last(), "duplicated")
    }

    private fun assertFile(file: Either<ExtFile, ExtFileTable>, type: String) {
        assertThat(file.isLeft()).isTrue()
        file.ifLeft {
            assertThat(it.fileName).isEqualTo(TEST_FILE_NAME)
            assertThat(it.attributes).hasSize(1)
            assertThat(it.attributes.first().name).isEqualTo("type")
            assertThat(it.attributes.first().value).isEqualTo(type)
        }
    }
}
