package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToExtStatTest {
    @Test
    fun `to ext stat`() {
        val stat = DocStat("VIEWS", 123)
        val extStat = stat.toExtStat()

        assertThat(extStat.name).isEqualTo("VIEWS")
        assertThat(extStat.value).isEqualTo("123")
    }
}
