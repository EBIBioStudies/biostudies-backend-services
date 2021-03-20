package ac.uk.ebi.biostd.persistence.doc.model

import ebi.ac.uk.model.constants.SectionFields
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DocSectionExtTest {
    @Test
    fun `section title`() {
        val section = DocSection(
            type = "Study",
            attributes = listOf(
                DocAttribute("Author", "Me"),
                DocAttribute(SectionFields.TITLE.value, "Test Title")))

        assertThat(section.title).isEqualTo("Test Title")
    }

    @Test
    fun `section with no title`() {
        val section = DocSection(
            type = "Study",
            attributes = listOf(DocAttribute("Author", "Me")))

        assertThat(section.title).isNull()
    }

    @Test
    fun `section with no attributes`() {
        val section = DocSection(type = "Study")
        assertThat(section.title).isNull()
    }
}
