package ac.uk.ebi.biostd.persistence.model.ext

import ac.uk.ebi.biostd.persistence.model.DbAttribute
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFileAttribute
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class ReferencedFileExtTest {
    @Test
    fun `valid attributes`() {
        val referencedFile = DbReferencedFile("file1.txt", 0).apply {
            attributes.add(DbReferencedFileAttribute(DbAttribute("Invalid", "", 0)))
            attributes.add(DbReferencedFileAttribute(DbAttribute("Valid", "Value", 1)))
        }

        val validAttributes = referencedFile.validAttributes
        Assertions.assertThat(validAttributes).hasSize(1)
        Assertions.assertThat(validAttributes.first().name).isEqualTo("Valid")
        Assertions.assertThat(validAttributes.first().value).isEqualTo("Value")
    }
}
