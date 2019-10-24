package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.simpleSection
import arrow.core.Either
import arrow.core.Either.Companion.right
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.second
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@Nested
@ExtendWith(MockKExtension::class)
class SectionsTables {
    private val fileSource = mockk<FilesSource>()
    private val simpleExtSection = mockk<ExtSection>()
    private val tableExtSection = mockk<ExtSection>()
    private val anotherTableExtSection = mockk<ExtSection>()

    private val section = simpleSection.apply { order = 0; tableIndex = NO_TABLE_INDEX }
    private val tableSection = simpleSection.apply { order = 1; tableIndex = 0; }
    private val anotherTableSection = simpleSection.apply { order = 2; tableIndex = 1 }

    private val sections = sortedSetOf(section, tableSection, anotherTableSection)

    @Test
    fun `Sections to ExtSectionTable`() {
        val sections = sections.toExtSections(fileSource)

        assertThat(sections.first()).isEqualTo(Either.left(simpleExtSection))
        assertThat(sections.second()).isEqualTo(right(ExtSectionTable(listOf(tableExtSection, anotherTableExtSection))))
    }
}
