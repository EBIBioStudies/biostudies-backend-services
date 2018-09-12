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
        val publicView = JsonSerializer().serialize(subm)
        val internalView = JsonSerializer().serializeWithInternalData(subm)

        assertThat(internalView).contains(subm.user.email)
        assertThat(internalView).contains(subm.user.id)

        assertThat(publicView).doesNotContain(subm.user.email)
        assertThat(publicView).doesNotContain(subm.user.id)
    }

    @Test
    fun `ignore null and empty properties`() {
        val out = JsonSerializer().serialize(submission{})
        assertThat(out).doesNotContain("accNo")
    }

    @Test
    fun `deserialize sample submission`() {
        val original = createVenousBloodMonocyte()
        val subm = JsonSerializer().deserialize(JsonSerializer().serialize(original), Submission::class.java)

        assertThat(subm).isNotNull
        assertThat(subm).isEqualTo(original)
    }
}
