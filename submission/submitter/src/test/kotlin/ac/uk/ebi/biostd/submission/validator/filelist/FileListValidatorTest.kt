package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.common.SerializationConfig
import ebi.ac.uk.dsl.excel.excel
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.io.sources.PathSource
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class FileListValidatorTest(
    private val tempFolder: TemporaryFolder,
) {
    private val filesSource = FileSourcesList(listOf(PathSource("Description", tempFolder.root.toPath())))
    private val testInstance = FileListValidator(SerializationConfig.serializationService())

    @BeforeAll
    fun beforeAll() {
        tempFolder.createFile("ref.txt")
    }

    @Test
    fun `valid file list`() {
        val content = tsv {
            line("Files", "Type")
            line("ref.txt", "test")
        }
        tempFolder.createFile("valid.tsv", content.toString())

        testInstance.validateFileList("valid.tsv", filesSource)
    }

    @Test
    fun `invalid file list`() {
        excel(tempFolder.createFile("fail.xlsx")) {
            sheet("page tab") {
                row {
                    cell("Files")
                    cell("Type")
                }
                row {
                    cell("ref.txt")
                    cell("test")
                }
                row {
                    cell("ghost.txt")
                    cell("fail")
                }
            }
        }

        assertThrows<FilesProcessingException> { testInstance.validateFileList("fail.xlsx", filesSource) }
    }
}
