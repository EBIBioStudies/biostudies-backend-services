package ebi.ac.uk.extended.mapping.serialization.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.File
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToFileListTest(
    @MockK val file: File,
    @MockK val systemFile: SystemFile,
    @MockK val extFile: ExtFile
) {
    private val extFileList = ExtFileList("fileList", systemFile, listOf(extFile))

    @Test
    fun toExtFileList() {
        mockkStatic(TO_FILE_EXTENSIONS) {
            every { extFile.toFile() } returns file

            val fileList = extFileList.toFileList()
            assertThat(fileList.referencedFiles.first()).isEqualTo(file)
            assertThat(fileList.name).isEqualTo(this.extFileList.fileName)
        }
    }
}
