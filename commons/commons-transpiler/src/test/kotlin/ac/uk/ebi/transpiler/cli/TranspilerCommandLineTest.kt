package ac.uk.ebi.transpiler.cli

import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.transpiler.service.FilesTableTemplateTranspiler
import com.github.ajalt.clikt.core.IncorrectOptionValueCount
import com.github.ajalt.clikt.core.MissingParameter
import com.github.ajalt.clikt.core.PrintMessage
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class TranspilerCommandLineTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockTranspiler: FilesTableTemplateTranspiler
) {
    @SpyK
    private var testInstance = TranspilerCommandLine(mockTranspiler)

    @BeforeEach
    fun setUp() {
        temporaryFolder.createFile("template.tsv")
        every { mockTranspiler.transpile("", listOf("colA", "colB"), "/path", "base", SubFormat.TSV) } returns ""
    }

    @AfterEach
    fun tearDown() = clearAllMocks()

    @Test
    fun `transpile template`() {
        val args = arrayOf(
            "-b", "base",
            "-f", "TSV",
            "-d", "/path",
            "-c", "colA, colB",
            "-t", "${temporaryFolder.root.absolutePath}/template.tsv"
        )
        testInstance.main(args)

        verify(exactly = 1) { mockTranspiler.transpile("", listOf("colA", "colB"), "/path", "base", SubFormat.TSV) }
    }

    @Test
    fun `transpiler exception`() {
        every {
            mockTranspiler.transpile("", listOf("colA", "colB"), "/path", "base", SubFormat.TSV)
        } throws Exception("Some exception")

        val args = arrayOf(
            "-b", "base",
            "-f", "TSV",
            "-d", "/path",
            "-c", "colA, colB",
            "-t", "${temporaryFolder.root.absolutePath}/template.tsv"
        )
        val exceptionMessage = assertThrows<PrintMessage> { testInstance.parse(args) }.message

        assertThat(exceptionMessage).isEqualTo("Some exception")
    }

    @Test
    fun `missing options`() {
        val exceptionMessage = assertThrows<MissingParameter> { testInstance.parse(arrayOf("-f", "JSON")) }.message
        assertThat(exceptionMessage).isEqualTo("Missing option \"--directory\".")
    }

    @Test
    fun `option with no value`() {
        val exceptionMessage = assertThrows<IncorrectOptionValueCount> { testInstance.parse(arrayOf("-f")) }.message
        assertThat(exceptionMessage).isEqualTo("-f option requires an argument")
    }
}
