package ebi.ac.uk.extended.mapping.serialization.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtFileListTest(
    @MockK val fileSource: FilesSource,
    @MockK val file: File,
    @MockK val systemFile: SystemFile,
    @MockK val extFile: ExtFile
) {
    private val fileList = FileList("fileList", listOf(file))

    @Test
    fun toExtFileList() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS) {
            every { file.toExtFile(fileSource) } returns extFile
            every { fileSource.getFile(fileList.name) } returns systemFile

            val extFileList = fileList.toExtFileList(fileSource)
            assertThat(extFileList.referencedFiles.first()).isEqualTo(extFile)
            assertThat(extFileList.fileName).isEqualTo(fileList.name)
            assertThat(extFileList.file).isEqualTo(systemFile)
        }
    }
}
