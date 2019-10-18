package ac.uk.ebi.biostd.persistence.mapping.extended.to

import ac.uk.ebi.biostd.persistence.model.ReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import io.mockk.spyk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File as SystemFile

@ExtendWith(MockKExtension::class)
internal class ToExtFileListTest(
    @MockK val fileSource: FilesSource,
    @MockK val systemFile: SystemFile,
    @MockK val extFile: ExtFile
) {

    private var file: ReferencedFile = spyk(ReferencedFile("file", 1))
    private val fileList = ReferencedFileList("fileList", sortedSetOf(file))

    @Test
    fun toExtFileList() {
        mockkStatic(TO_EXT_FILE_EXTENSIONS) {
            every { file.toExtFile(fileSource) } returns extFile
            every { fileSource.getFile(fileList.name) } returns systemFile
            every { fileSource.getFile(file.name) } returns systemFile

            val extFileList = fileList.toExtFileList(fileSource)
            assertThat(extFileList.files.first()).isEqualTo(extFile)
            assertThat(extFileList.fileName).isEqualTo(fileList.name)
            assertThat(extFileList.file).isEqualTo(systemFile)
        }
    }
}
