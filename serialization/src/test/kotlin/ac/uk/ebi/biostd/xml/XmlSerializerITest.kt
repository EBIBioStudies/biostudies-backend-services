package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.assertj.core.api.Assertions.assertThat

class XmlSerializerITest {

    private val testInstance = XmlSerializer()

    private val subm = createVenousBloodMonocyte()

    @org.junit.jupiter.api.Test
    fun `serialize and deserialize sample submission`() {
        val xml = testInstance.serialize(subm)
        val result = testInstance.deserialize(xml)

        assertThat(result).isEqualTo(subm)
    }
}
