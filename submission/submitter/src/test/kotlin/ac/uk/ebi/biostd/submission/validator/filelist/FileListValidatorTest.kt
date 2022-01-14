package ac.uk.ebi.biostd.submission.validator.filelist

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.persistence.common.exception.FileListNotFoundException
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class FileListValidatorTest(
    @MockK private val fileList: FileList,
    @MockK private val referencedFile: File,
    @MockK private val filesSource: FilesSource,
    @MockK private val serializationService: SerializationService
) {
    private val testInstance = FileListValidator(serializationService)

    @BeforeEach
    fun beforeEach() {
        every { referencedFile.path } returns "referenced/file/path.txt"
        every { serializationService.deserializeFileList("file-list", filesSource) } returns fileList
    }

    @Test
    fun `validate file list`() {
        every { filesSource.exists("file-list") } returns true
        testInstance.validateFileList("file-list", filesSource)
    }

    @Test
    fun `validate invalid file list`() {
        every { filesSource.exists("file-list") } returns false

        val exception = assertThrows<FileListNotFoundException> { testInstance.validateFileList("file-list", filesSource) }
        assertThat(exception.message).isEqualTo("The file list 'file-list' could not be found")
    }
}
