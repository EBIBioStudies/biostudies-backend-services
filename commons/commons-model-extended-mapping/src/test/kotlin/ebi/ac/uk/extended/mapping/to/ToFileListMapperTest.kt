package ebi.ac.uk.extended.mapping.to

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToFileListMapperTest(
    temporaryFolder: TemporaryFolder,
    @MockK val serializationService: ExtSerializationService,
    @MockK val file: File,
    @MockK val extFile: ExtFile
) {
    private val extFileList = ExtFileList("fileList", temporaryFolder.createFile("file-list.json"))
    private val testInstance = ToFileListMapper(serializationService)

    @Test
    fun toExtFileList() {
        mockkStatic(TO_FILE_EXTENSIONS) {
            every { serializationService.deserializeList(any()) } returns sequenceOf(extFile)
            every { extFile.toFile() } returns file

            val fileList = testInstance.convert(extFileList)

            assertThat(fileList.referencedFiles.first()).isEqualTo(file)
            assertThat(fileList.name).isEqualTo(this.extFileList.filePath)
        }
    }
}
