package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class JsonSerializerTest {

    private val testInstance = JsonSerializer()

    private val subm = createVenousBloodMonocyte()

    @Test
    fun `serialize sample submission`() {
        val out = JsonSerializer().serialize(subm)
        assertThat(out).isNotNull()
    }

    /*
    @Test
    fun `serialize sample submission with internal data`() {
        subm.user = User("user@email.com")//, "#42")
        val publicJson = testInstance.serialize(subm)
        val internalJson = testInstance.serializeWithInternalData(subm)

        assertThat(internalJson).contains(subm.user.email)
        assertThat(internalJson).contains(subm.user.id)

        assertThat(publicJson).doesNotContain(subm.user.email)
        assertThat(publicJson).doesNotContain(subm.user.id)
    }*/

    @Test
    @Disabled("change for real json submission")
    fun `deserialize sample submission`() {
        val json = testInstance.serialize(subm)
        val deserialized = testInstance.deserialize(json, Submission::class.java)

        assertThat(deserialized).isNotNull
        assertThat(deserialized).isEqualTo(subm)
    }
}
