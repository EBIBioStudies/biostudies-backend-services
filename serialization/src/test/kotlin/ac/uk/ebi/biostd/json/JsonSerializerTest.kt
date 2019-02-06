package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * TODO: add extended submission test
 * TODO: implements property equals and hashcode method to avoid comparison using isEqualToComparingFieldByField
 */
class JsonSerializerTest {

    private val testInstance = JsonSerializer.mapper

    private val subm = createVenousBloodMonocyte()

    @Test
    fun `serialize and deserialize sample submission`() {
        val json = testInstance.writeValueAsString(subm)
        val deserialized = testInstance.readValue(json, Submission::class.java)

        assertThat(deserialized).isNotNull
        assertThat(deserialized).isEqualToComparingFieldByField(subm)
    }
}
