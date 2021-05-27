package ac.uk.ebi.biostd.tsv

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ac.uk.ebi.biostd.test.twoBasicSubmission
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class TsvSerializerTest {
    private val testSubmission = createVenousBloodMonocyte()
    private val testInstance: TsvSerializer = TsvSerializer()

    @Test
    fun `serialize and deserialize submission`() {
        val tsv = testInstance.serialize(testSubmission)
        val result = testInstance.deserialize(tsv)

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(testSubmission)
    }

    @Test
    fun deserializeList() {
        val submissions = testInstance.deserializeList(twoBasicSubmission())

        assertThat(submissions).hasSize(2)
        submissions.forEach {
            assertThat(it).isEqualTo(
                submission("S-EPMC123") {
                    attribute("Title", "Basic Submission")
                    attribute("DataSource", "EuropePMC")
                    attribute("AttachTo", "EuropePMC")
                }
            )
        }
    }
}
