package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.DbSubmissionAttribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SubmissionExtTest {
    @Test
    fun `valid attributes`() {
        val submission = DbSubmission().apply {
            attributes.add(DbSubmissionAttribute(DbAttribute("Invalid", "", 0)))
            attributes.add(DbSubmissionAttribute(DbAttribute("Valid", "Value", 1)))
        }

        val validAttributes = submission.validAttributes
        assertThat(validAttributes).hasSize(1)
        assertThat(validAttributes.first().name).isEqualTo("Valid")
        assertThat(validAttributes.first().value).isEqualTo("Value")
    }
}
