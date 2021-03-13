package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Submission
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JsonSerializerTest {
    private val testInstance = JsonSerializer.mapper
    private val submission = createVenousBloodMonocyte()

    @Test
    fun `serialize and deserialize sample submission`() {
        val json = testInstance.writeValueAsString(submission)
        val deserialized = testInstance.readValue(json, Submission::class.java)

        assertThat(deserialized).isNotNull
        assertThat(deserialized).isEqualTo(submission)
    }
}
