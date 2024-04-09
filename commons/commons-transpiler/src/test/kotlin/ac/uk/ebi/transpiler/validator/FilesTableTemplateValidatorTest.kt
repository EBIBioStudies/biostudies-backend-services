package ac.uk.ebi.transpiler.validator

import ac.uk.ebi.transpiler.exception.InvalidDirectoryException
import ac.uk.ebi.transpiler.factory.filesTableTemplate
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class FilesTableTemplateValidatorTest(private val temporaryFolder: TemporaryFolder) {
    private lateinit var rootPath: String
    private val template = filesTableTemplate()
    private val testInstance = FilesTableTemplateValidator()

    @BeforeEach
    fun setUp() {
        rootPath = temporaryFolder.root.absolutePath

        setUpTestFiles(1)
        setUpTestFiles(2)
    }

    @Test
    fun validate() {
        testInstance.validate(template, rootPath)
    }

    @Test
    fun `validate non existing folder`() {
        template.addRecord("Plate3/rep3/A03", listOf())

        val exception = assertThrows<InvalidDirectoryException> { testInstance.validate(template, rootPath) }

        assertThat(exception.message).isEqualTo(
            "The following directories don't exist or don't contain files: [$rootPath/Plate3/rep3/A03]",
        )
    }

    @Test
    fun `validate empty folder`() {
        temporaryFolder.createDirectory("Plate3")
        template.addRecord("Plate3/rep3/A03", listOf())

        val exception = assertThrows<InvalidDirectoryException> { testInstance.validate(template, rootPath) }

        assertThat(exception.message).isEqualTo(
            "The following directories don't exist or don't contain files: [$rootPath/Plate3/rep3/A03]",
        )
    }

    @Test
    fun `validate non existing and empty folders`() {
        temporaryFolder.createDirectory("Plate3")
        template.addRecord("Plate3/rep3/A03", listOf())
        template.addRecord("Plate4/rep4/A04", listOf())

        val failingDirs = "[$rootPath/Plate3/rep3/A03, $rootPath/Plate4/rep4/A04]"
        val exception = assertThrows<InvalidDirectoryException> { testInstance.validate(template, rootPath) }

        assertThat(exception.message).isEqualTo(
            "The following directories don't exist or don't contain files: $failingDirs",
        )
    }

    private fun setUpTestFiles(idx: Int) {
        temporaryFolder.createDirectory("Plate$idx")
        temporaryFolder.createDirectory("Plate$idx/rep$idx")
        temporaryFolder.createDirectory("Plate$idx/rep$idx/A0$idx")
        temporaryFolder.createFile("Plate$idx/rep$idx/A0$idx/fig1.tiff")
    }
}
