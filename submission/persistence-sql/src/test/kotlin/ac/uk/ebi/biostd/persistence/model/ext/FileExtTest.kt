package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbFileAttribute
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FileExtTest {
    @Test
    fun `is table`() {
        val file = DbFile("file1.txt", 0).apply { tableIndex = 1 }
        assertThat(file.isTableElement()).isTrue()
    }

    @Test
    fun `is not table`() {
        val file = DbFile("file1.txt", 0)
        assertThat(file.isTableElement()).isFalse()
    }

    @Test
    fun `valid attributes`() {
        val file = DbFile("file1.txt", 0).apply {
            attributes.add(DbFileAttribute(DbAttribute("Invalid", "", 0)))
            attributes.add(DbFileAttribute(DbAttribute("Valid", "Value", 1)))
        }

        val validAttributes = file.validAttributes
        assertThat(validAttributes).hasSize(1)
        assertThat(validAttributes.first().name).isEqualTo("Valid")
        assertThat(validAttributes.first().value).isEqualTo("Value")
    }
}
