package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class XmlSerializerITest {
    private val testInstance = XmlSerializer()
    private val submission = createVenousBloodMonocyte()

    @Test
    fun `serialize and deserialize sample submission`() {
        val xml = testInstance.serialize(submission)
        val result = testInstance.deserialize(xml)

        assertThat(result.accNo).isEqualTo(submission.accNo)
        assertThat(result.section).isEqualTo(submission.section)
        assertThat(result.attributes).isEqualTo(submission.attributes)
    }
}
