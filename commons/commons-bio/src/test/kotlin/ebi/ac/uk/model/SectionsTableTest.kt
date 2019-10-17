package ebi.ac.uk.model

import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SectionsTableTest {
    @Test
    fun `as sections table`() {
        val section = Section("Study", "S1")
        val extendedSection = ExtendedSection(Section("Experiment", "E1"))
        val sectionsTable = SectionsTable(listOf(section, extendedSection)).asSectionsTable()

        assertThat(sectionsTable.elements).hasSize(2)
        assertThat(sectionsTable.elements.first()).isEqualTo(section)
        assertThat(sectionsTable.elements.second()).isEqualTo(extendedSection.asSection())
    }
}
