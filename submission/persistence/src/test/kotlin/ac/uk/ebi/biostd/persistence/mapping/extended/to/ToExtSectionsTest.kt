package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.assertExtSection
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.sectionDb
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.second
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToExtSectionsTest {
    private val fileSource = mockk<FilesSource>()

    private val section = sectionDb.apply { accNo = "1"; order = 0; tableIndex = NO_TABLE_INDEX }
    private val tableSection = sectionDb.apply { accNo = "2"; order = 1; tableIndex = 0; }
    private val anotherTableSection = sectionDb.apply { accNo = "3"; order = 2; tableIndex = 1 }

    private val sections = sortedSetOf(section, tableSection, anotherTableSection)

    @Test
    fun `Sections to ExtSectionTable`() {
        val sections = sections.toExtSections(fileSource)

        assertThat(sections.first()).hasLeftValueSatisfying { assertExtSection(section, it) }
        assertThat(sections.second()).hasRightValueSatisfying { table ->
            assertThat(table.sections).hasSize(2)
            assertExtSection(tableSection, table.sections.first())
            assertExtSection(anotherTableSection, table.sections.second())
        }
    }
}
