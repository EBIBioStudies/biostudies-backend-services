package ac.uk.ebi.transpiler.cli

import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.transpiler.service.FilesTableTemplateTranspiler
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.commons.cli.HelpFormatter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class TranspilerCommandLineTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK private val mockHelpFormatter: HelpFormatter,
    @MockK private val mockTranspiler: FilesTableTemplateTranspiler
) {
    @SpyK
    private var testInstance = TranspilerCommandLine(mockHelpFormatter, mockTranspiler)

    private val options = testInstance.options

    @BeforeEach
    fun setUp() {
        temporaryFolder.createFile("template.tsv")
        every { mockHelpFormatter.printHelp(CLI_ID, options) }.answers { nothing }
        every { mockTranspiler.transpile("", listOf("colA", "colB"), "/path", "base", SubFormat.TSV) }.returns("pageTab")
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
            "-t", "${temporaryFolder.root.absolutePath}/template.tsv")
        val pageTab = testInstance.transpile(args)

        assertThat(pageTab).isEqualTo("pageTab")
        verify(exactly = 0) { testInstance.printError("") }
        verify(exactly = 0) { mockHelpFormatter.printHelp(CLI_ID, options) }
    }

    @Test
    fun `missing arguments`() {
        val pageTab = testInstance.transpile(arrayOf("-f", "JSON"))

        assertThat(pageTab).isEqualTo("")
        verify(exactly = 1) { mockHelpFormatter.printHelp(CLI_ID, options) }
        verify(exactly = 1) { testInstance.printError("Missing required options: b, c, d, t") }
    }

    @Test
    fun `argument with no value`() {
        val pageTab = testInstance.transpile(arrayOf("-b"))

        assertThat(pageTab).isEqualTo("")
        verify(exactly = 1) { mockHelpFormatter.printHelp(CLI_ID, options) }
        verify(exactly = 1) { testInstance.printError("Missing argument for option: b") }
    }
}
