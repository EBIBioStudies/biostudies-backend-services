package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonSerializerTest {

    @Test
    fun `serialize sample submission`() {
        val out: String = JsonSerializer().serialize(createVenousBloodMonocyte())
        assertThat(out).isNotNull()
    }

    @Test
    fun `deserialize sample submission`() {
        val original = createVenousBloodMonocyte()
        val subm = JsonSerializer().deserialize(JsonSerializer().serialize(original), Submission::class.java)

        assertThat(subm).isNotNull
        assertThat(subm).isEqualTo(original)
    }
}
