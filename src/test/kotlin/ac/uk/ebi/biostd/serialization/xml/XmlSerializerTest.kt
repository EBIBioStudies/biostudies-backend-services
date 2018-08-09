package ac.uk.ebi.biostd.serialization.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.junit.Test

class XmlSerializerTest {

    private val testInstance = XmlSerializer()

    @Test
    fun serialize() {
        val sub = createVenousBloodMonocyte()
        val tsvString = testInstance.serialize(sub)
    }

}
