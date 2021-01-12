package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbSection
import ac.uk.ebi.biostd.persistence.model.DbSectionAttribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SectionExtTest {
    @Test
    fun `table element`() {
        val section = DbSection("SECT-001", "Study").apply { tableIndex = 0 }
        assertTrue { section.isTableElement() }
    }

    @Test
    fun `not table element`() {
        val section = DbSection("SECT-001", "Study")
        assertFalse { section.isTableElement() }
    }

    @Test
    fun `section title`() {
        val section = DbSection("SECT-001", "Study").apply {
            attributes.add(DbSectionAttribute(DbAttribute("Title", "Section Title", 0)))
        }

        assertThat(section.title).isEqualTo("Section Title")
    }

    @Test
    fun `section without title`() {
        val section = DbSection("SECT-001", "Study")
        assertThat(section.title).isNull()
    }

    @Test
    fun `valid attributes`() {
        val section = DbSection("SECT-001", "Study").apply {
            attributes.add(DbSectionAttribute(DbAttribute("Invalid", "", 0)))
            attributes.add(DbSectionAttribute(DbAttribute("Valid", "Value", 1)))
        }

        val validAttributes = section.validAttributes
        assertThat(validAttributes).hasSize(1)
        assertThat(validAttributes.first().name).isEqualTo("Valid")
        assertThat(validAttributes.first().value).isEqualTo("Value")
    }
}
