package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.common.NO_TABLE_INDEX
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.simpleFile
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.simpleLink
import ac.uk.ebi.biostd.persistence.mapping.extended.to.test.simpleSection
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.util.collections.second
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtTableTest {
    @Nested
    @ExtendWith(MockKExtension::class)
    inner class LinksTables {
        private val simpleExtLink = mockk<ExtLink>()
        private val tableExtLink = mockk<ExtLink>()
        private val anotherTableExtLink = mockk<ExtLink>()

        private val link = simpleLink.apply { tableIndex = NO_TABLE_INDEX; order = 0 }
        private val tableLink = simpleLink.apply { tableIndex = 0; order = 1 }
        private val anotherTableLink = simpleLink.apply { tableIndex = 1; order = 2 }

        private val links = sortedSetOf(link, tableLink, anotherTableLink)

        @Test
        fun `Links to ExtLinkTable`() {
            mockkStatic(TO_EXT_LINK_EXTENSIONS) {
                every { link.toExtLink() } returns simpleExtLink
                every { tableLink.toExtLink() } returns tableExtLink
                every { anotherTableLink.toExtLink() } returns anotherTableExtLink

                val links = links.toExtLinks()
                assertThat(links.first()).isEqualTo(Either.left(simpleExtLink))
                assertThat(links.second()).isEqualTo(Either.right(ExtLinkTable(listOf(tableExtLink, anotherTableExtLink))))
            }
        }
    }

    @Nested
    @ExtendWith(MockKExtension::class)
    inner class FilesTables {
        private val filesSource = mockk<FilesSource>()
        private val simpleExtFile = mockk<ExtFile>()
        private val tableExtFile = mockk<ExtFile>()
        private val anotherTableExtFile = mockk<ExtFile>()

        private val file = simpleFile.apply { tableIndex = NO_TABLE_INDEX; order = 0 }
        private val tableFile = simpleFile.apply { tableIndex = 0; order = 1 }
        private val anotherTableFile = simpleFile.apply { tableIndex = 1; order = 2 }

        private val files = sortedSetOf(file, tableFile, anotherTableFile)

        @Test
        fun `Files to ExtFileTable`() {
            mockkStatic(TO_EXT_FILE_EXTENSIONS) {
                every { file.toExtFile(filesSource) } returns simpleExtFile
                every { tableFile.toExtFile(filesSource) } returns tableExtFile
                every { anotherTableFile.toExtFile(filesSource) } returns anotherTableExtFile

                val files = files.toExtFiles(filesSource)
                assertThat(files.first()).isEqualTo(Either.left(simpleExtFile))
                assertThat(files.second()).isEqualTo(Either.right(ExtFileTable(listOf(tableExtFile, anotherTableExtFile))))
            }
        }
    }

    @Nested
    @ExtendWith(MockKExtension::class)
    inner class SectionsTables {
        private val fileSource = mockk<FilesSource>()
        private val simpleExtSection = mockk<ExtSection>()
        private val tableExtSection = mockk<ExtSection>()
        private val anotherTableExtSection = mockk<ExtSection>()

        private val section = simpleSection.apply { tableIndex = NO_TABLE_INDEX; order = 0 }
        private val tableSection = simpleSection.apply { tableIndex = 0; order = 1 }
        private val anotherTableSection = simpleSection.apply { tableIndex = 1; order = 2 }

        private val sections = sortedSetOf(section, tableSection, anotherTableSection)

        @Test
        fun `Sections to ExtSectionTable`() {
            mockkStatic(TO_EXT_SECTION_EXTENSIONS) {
                every { section.toExtSection(fileSource) } returns simpleExtSection
                every { tableSection.toExtSection(fileSource) } returns tableExtSection
                every { anotherTableSection.toExtSection(fileSource) } returns anotherTableExtSection

                val sections = sections.toExtSections(fileSource)
                assertThat(sections.first()).isEqualTo(Either.left(simpleExtSection))
                assertThat(sections.second()).isEqualTo(Either.right(ExtSectionTable(listOf(tableExtSection, anotherTableExtSection))))
            }
        }
    }
}
