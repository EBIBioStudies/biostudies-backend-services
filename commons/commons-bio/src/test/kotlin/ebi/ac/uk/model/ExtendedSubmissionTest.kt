package ebi.ac.uk.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

const val ACC_NO = "ABC456"
const val ACCESS_TAG = "Public"

class ExtendedSubmissionTest {
    private val rootSection = Section()
    private val attribute = Attribute("Name", "Value")
    private val user = User(123L, "user@mail.com", "SecretKey")

    @Test
    fun `create empty extended submission`() {
        val extendedSubmission = ExtendedSubmission(ACC_NO, user)

        assertThat(extendedSubmission.accNo).isEqualTo(ACC_NO)
        assertThat(extendedSubmission.user).isEqualTo(user)
    }

    @Test
    fun `create extended submission from basic submission`() {
        val submission = Submission(ACC_NO, rootSection, listOf(attribute))
        submission.accessTags.add(ACCESS_TAG)

        val extendedSubmission = ExtendedSubmission(submission, user)

        assertThat(extendedSubmission.accNo).isEqualTo(ACC_NO)
        assertThat(extendedSubmission.user).isEqualTo(user)
        assertThat(extendedSubmission.section).isEqualTo(rootSection)
        assertThat(extendedSubmission.attributes).contains(attribute)
        assertThat(extendedSubmission.accessTags).contains(ACCESS_TAG)
    }
}
