package ebi.ac.uk.extended.model

import arrow.core.Either.Companion.left
import arrow.core.Either.Companion.right
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ExtSectionExtensionsTest {
    @Test
    fun getAllSections() {
        val tableSec = mockk<ExtSection>()
        val simpleSection = mockk<ExtSection>()
        every { simpleSection.sections } returns emptyList()

        val section = ExtSection(
            type = "study",
            sections = listOf(left(simpleSection), right(ExtSectionTable(listOf(tableSec))))
        )

        assertThat(section.allSections).containsExactly(simpleSection, tableSec)
    }

    @Test
    fun getAllFileListFiles() {
        val file = mockk<ExtFile>()
        val listFile = mockk<ExtFile>()
        val fileList = mockk<ExtFileList>()

        every { fileList.files } returns listOf(listFile)

        val section = ExtSection(type = "study", files = listOf(left(file)), fileList = fileList)
        assertThat(section.allReferencedFiles).containsExactly(listFile)
    }

    @Test
    fun getAllFiles() {
        val tableFile = mockk<ExtFile>()
        val simpleFile = mockk<ExtFile>()

        val fileList = mockk<ExtFileList>()
        val listFile = mockk<ExtFile>()
        every { fileList.files } returns listOf(listFile)

        val section = ExtSection(
            type = "study",
            files = listOf(left(simpleFile), right(ExtFileTable(listOf(tableFile)))),
            fileList = fileList
        )

        assertThat(section.allFiles).containsExactly(simpleFile, tableFile)
    }
}
