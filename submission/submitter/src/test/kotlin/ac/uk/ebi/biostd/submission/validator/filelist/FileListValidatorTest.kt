package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.PageTabFileReader
import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException
import ac.uk.ebi.biostd.submission.exceptions.InvalidFilesException.Companion.buildMessage
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
class FileListValidatorTest(
    temporaryFolder: TemporaryFolder,
    @MockK private val filesSource: FilesSource,
    @MockK private val serializationService: SerializationService
) {

    private val excelFile: File = mockk()
    private val fileListFile = temporaryFolder.createFile("file-list.tsv", tsvFileContent)
    private val testInstance = FileListValidator(serializationService)

    @BeforeEach
    fun beforeEach() {
        mockkObject(PageTabFileReader)
        every { filesSource.getSystemFile("file-list.xlsx") } returns excelFile
        every { PageTabFileReader.getPageTabFile(excelFile) } returns Pair(fileListFile, TSV)
        every { serializationService.deserializeFileList(any(), TSV) } returns deserializedFileList
    }

    @Test
    fun `validation file list`() {
        (1..130).onEach { every { filesSource.exists("file$it.txt") } returns true }

        testInstance.validateFileList("file-list.xlsx", filesSource)
    }

    @Test
    fun `invalid validation file list more than 100 non-existing files`() {
        (1..10).onEach { every { filesSource.exists("file$it.txt") } returns true }
        (11..120).onEach { every { filesSource.exists("file$it.txt") } returns false }
        (121..130).onEach { every { filesSource.exists("file$it.txt") } returns true }

        val exception =
            assertThrows<InvalidFilesException> { testInstance.validateFileList("file-list.xlsx", filesSource) }
        assertThat(exception.message)
            .isEqualTo(buildMessage("file-list.xlsx", deserializedFileList.drop(10).take(100).toList()))
    }

    companion object {
        val tsvFileContent = tsv {
            line("Files", "Attr 1", "Attr 2")
            (1..130).forEach { line("file$it.txt", "a1-$it", "a2-$it") }
        }.toString()
        val deserializedFileList = sequence {
            (1..110).forEach {
                yield(
                    ebi.ac.uk.model.File(
                        path = "file$it.txt",
                        attributes = listOf(Attribute("Attr 1", "a1-$it"), Attribute("Attr 2", "a2-$it"))
                    )
                )
            }
        }
    }
}
