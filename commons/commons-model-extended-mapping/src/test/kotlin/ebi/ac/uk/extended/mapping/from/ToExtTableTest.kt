package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class ToExtTableTest {
    @Test
    fun `FileTable toExtTable`(
        @MockK filesSource: FilesSource,
        @MockK file: BioFile,
        @MockK fileTable: FilesTable,
        @MockK extFile: ExtFile
    ) {
        mockkStatic(TO_EXT_FILE_EXTENSIONS) {
            every { file.toExtFile(filesSource) } returns extFile
            every { fileTable.elements } returns listOf(file)

            val result = fileTable.toExtTable(filesSource)

            assertThat(result.files).containsExactly(extFile)
        }
    }

    @Test
    fun `LinkTable toExtTable`(
        @MockK tableLink: Link,
        @MockK linkTable: LinksTable,
        @MockK extLink: ExtLink
    ) {
        mockkStatic(TO_EXT_LINK_EXTENSIONS) {
            every { tableLink.toExtLink() } returns extLink
            every { linkTable.elements } returns listOf(tableLink)

            val result = linkTable.toExtTable()

            assertThat(result.links).containsExactly(extLink)
        }
    }
}
