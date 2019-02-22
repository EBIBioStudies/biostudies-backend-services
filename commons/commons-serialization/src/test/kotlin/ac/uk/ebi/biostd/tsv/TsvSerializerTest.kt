package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ac.uk.ebi.biostd.test.twoBasicSubmission
import ebi.ac.uk.asserts.assertSubmission
import ebi.ac.uk.model.Attribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TsvSerializerTest {
    private val submission = createVenousBloodMonocyte()
    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun `serialize and deserialize submission`() {
        val tsv = testInstance.serialize(submission)
        val result = testInstance.deserialize(tsv)

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(submission)
    }

    @Test
    fun deserializeList() {
        val submissions = testInstance.deserializeList(twoBasicSubmission())

        assertThat(submissions).hasSize(2)
        submissions.forEach {
            assertSubmission(
                it,
                "S-EPMC123",
                Attribute("Title", "Basic Submission"),
                Attribute("DataSource", "EuropePMC"),
                Attribute("AttachTo", "EuropePMC"))
        }
    }
}
