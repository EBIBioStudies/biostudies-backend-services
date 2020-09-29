package uk.ac.ebi.fire.client.ext

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.ac.ebi.fire.client.model.MetadataEntry

class MetadataEntryExtTest {
    @Test
    fun `entries as request parameter`() {
        val meta = listOf(MetadataEntry("Meta1", "meta 1"), MetadataEntry("Meta2", "meta 2"))
        assertThat(meta.asRequestParameter()).isEqualTo("{ \"Meta1\": \"meta 1\",\"Meta2\": \"meta 2\" }")
    }
}
