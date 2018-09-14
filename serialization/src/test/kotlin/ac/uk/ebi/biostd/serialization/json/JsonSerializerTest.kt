package ac.uk.ebi.biostd.serialization.json

import ac.uk.ebi.biostd.submission.Submission
import ac.uk.ebi.biostd.submission.User
import ac.uk.ebi.biostd.submission.submission
import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonSerializerTest {

    @Test
    fun `serialize sample submission`() {
        val subm = createVenousBloodMonocyte()
        val out = JsonSerializer().serialize(subm)
        assertThat(out).isNotNull()
    }

    @Test
    fun `serialize sample submission with internal data`() {
        val subm = createVenousBloodMonocyte()
        subm.user = User("user@email.com", "#42")
        val publicJson = JsonSerializer().serialize(subm)
        val internalJson = JsonSerializer().serializeWithInternalData(subm)

        assertThat(internalJson).contains(subm.user.email)
        assertThat(internalJson).contains(subm.user.id)

        assertThat(publicJson).doesNotContain(subm.user.email)
        assertThat(publicJson).doesNotContain(subm.user.id)
    }

    @Test
    fun `ignore null and empty properties`() {
        val out = JsonSerializer().serialize(submission{})
        assertThat(out).doesNotContain("accNo")
    }

    @Test
    fun `deserialize sample submission`() {
        val original = createVenousBloodMonocyte()
        val json = JsonSerializer().serialize(original)
        val subm = JsonSerializer().deserialize(json, Submission::class.java)

        assertThat(subm).isNotNull
        assertThat(subm).isEqualTo(original)
    }
}
