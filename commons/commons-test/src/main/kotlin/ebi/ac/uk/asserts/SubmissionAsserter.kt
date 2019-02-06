package ebi.ac.uk.asserts

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat

fun assertSubmission(submission: Submission, accNo: String, vararg attributes: Attribute) {
    assertThat(submission.accNo).isEqualTo(accNo)
    assertThat(submission.attributes).isEqualTo(attributes.toList())
}
