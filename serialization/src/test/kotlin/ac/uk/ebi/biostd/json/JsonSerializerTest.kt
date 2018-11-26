package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class JsonSerializerTest {

    private val testInstance = JsonSerializer.mapper

    private val subm = createVenousBloodMonocyte()

    @Test
    fun `serialize sample submission`() {
        val out = JsonSerializer().serialize(subm)
        assertThat(out).isNotNull()
    }

    @Test
    @Disabled("change for real json submission")
    fun `deserialize sample submission`() {
        val json = testInstance.writeValueAsString(subm)
        val deserialized = testInstance.readValue(json, Submission::class.java)

        assertThat(deserialized).isNotNull
        assertThat(deserialized).isEqualTo(subm)
    }
}
