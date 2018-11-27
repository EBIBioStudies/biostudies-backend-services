package ebi.ac.uk.asserts

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.title
import org.assertj.core.api.Assertions.assertThat

fun assertSubmission(submission: Submission, accNo: String, title: String, vararg attributes: Attribute) {
    assertThat(submission.accNo).isEqualTo(accNo)
    assertThat(submission.title).isEqualTo(title)

    attributes.forEachIndexed { idx, attribute -> assertThat(attribute).isEqualTo(submission.attributes[idx]) }
}
