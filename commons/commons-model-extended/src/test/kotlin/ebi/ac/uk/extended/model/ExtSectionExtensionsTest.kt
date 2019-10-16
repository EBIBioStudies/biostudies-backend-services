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

        val section = ExtSection(
            type = "study",
            sections = listOf(left(simpleSection), right(ExtSectionTable(listOf(tableSec))))
        )

        assertThat(section.allSections).containsExactly(simpleSection, tableSec)
    }

    @Test
    fun getAllReferencedFiles() {
        val file = mockk<ExtFile>()
        val fileList = mockk<ExtFileList>()

        every { fileList.files } returns listOf(file)

        val section = ExtSection(type = "study", fileList = fileList)
        assertThat(section.allReferencedFiles).containsExactly(file)
    }

    @Test
    fun getAllFiles() {
        val tableFile = mockk<ExtFile>()
        val simpleFile = mockk<ExtFile>()

        val section = ExtSection(
            type = "study",
            files = listOf(left(simpleFile), right(ExtFileTable(listOf(tableFile))))
        )

        assertThat(section.allFiles).containsExactly(simpleFile, tableFile)
    }
}
