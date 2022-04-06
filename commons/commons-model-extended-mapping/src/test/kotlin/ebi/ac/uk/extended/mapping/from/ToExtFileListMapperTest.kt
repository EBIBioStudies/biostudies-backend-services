package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ToExtFileListMapperTest(
    @MockK val fileSource: FilesSource,
    @MockK val file: File,
    @MockK val systemFile: NfsBioFile,
    @MockK val extFile: ExtFile,
) {
    private val tesInstance = ToExtFileListMapper()
    private val fileList = FileList("fileList", listOf(file))

    @Test
    fun toExtFileList() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS) {
            every { file.toExtFile(fileSource, false) } returns extFile
            //every { fileSource.getFile(fileList.name) } returns systemFile

            val extFileList = tesInstance.convert(fileList, fileSource)

            assertThat(extFileList.files.first()).isEqualTo(extFile)
            assertThat(extFileList.filePath).isEqualTo(fileList.name)
        }
    }
}
