package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbLink
import ac.uk.ebi.biostd.persistence.model.DbLinkAttribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LinkExtTest {
    @Test
    fun `is table`() {
        val link = DbLink("CHEBI:74", 0).apply { tableIndex = 1 }
        assertThat(link.isTableElement()).isTrue()
    }

    @Test
    fun `is not table`() {
        val link = DbLink("CHEBI:74", 0)
        assertThat(link.isTableElement()).isFalse()
    }

    @Test
    fun `valid attributes`() {
        val link = DbLink("CHEBI:74", 0).apply {
            attributes.add(DbLinkAttribute(DbAttribute("Invalid", "", 0)))
            attributes.add(DbLinkAttribute(DbAttribute("Valid", "Value", 1)))
        }

        val validAttributes = link.validAttributes
        assertThat(validAttributes).hasSize(1)
        assertThat(validAttributes.first().name).isEqualTo("Valid")
        assertThat(validAttributes.first().value).isEqualTo("Value")
    }
}
