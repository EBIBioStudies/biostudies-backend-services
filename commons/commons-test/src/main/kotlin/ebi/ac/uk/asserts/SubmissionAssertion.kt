package ebi.ac.uk.asserts

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import org.assertj.core.api.AbstractAssert
import org.assertj.core.api.Assertions.assertThat

fun assertThat(submission: Submission): SubmissionAssert {
    return SubmissionAssert(submission)
}

class SubmissionAssert(actual: Submission) :
    AbstractAssert<SubmissionAssert, Submission>(actual, SubmissionAssert::class.java) {

    fun hasAccNo(accNo: String) {
        assertThat(actual.accNo).isEqualTo(accNo)
    }

    fun hasExactly(vararg attributes: Attribute) {
        assertThat(actual.attributes).containsExactly(*attributes)
    }
}
