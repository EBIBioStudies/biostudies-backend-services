package ebi.ac.uk.extended.mapping.to

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.model.BioFile
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
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.OutputStream

@ExtendWith(MockKExtension::class, TemporaryFolderExtension::class)
internal class ToFileListMapperTest(
    private val temporaryFolder: TemporaryFolder,
    @MockK val extSerializationService: ExtSerializationService,
    @MockK val serializationService: SerializationService,
    @MockK val filesResolver: FilesResolver,
    @MockK val extFile: ExtFile,
    @MockK val bioFile: BioFile
) {
    private val extFileList = ExtFileList("fileList", temporaryFolder.createFile("source-file-list.json"))
    private val testInstance = ToFileListMapper(serializationService, extSerializationService, filesResolver)

    @Test
    fun convert() {
        every { filesResolver.createEmptyFile("fileList") } returns temporaryFolder.createFile("target-file-list.json")
        every { serializationService.serializeFileList(any(), any(), any()) } answers {
            val sequence = arg<Sequence<BioFile>>(0)
            val format = arg<SubFormat>(1)
            val stream = arg<OutputStream>(2)
            stream.use { it.write("${sequence.toList().size}-$format".toByteArray()) }
        }

        val fileList = testInstance.convert(extFileList)

        assertThat(fileList.name).isEqualTo("fileList")
        assertThat(fileList.file).hasContent("0-JSON")
    }

    @Test
    fun serialize() {
        val target = temporaryFolder.createFile("target-file-list.json")
        mockkStatic(TO_FILE_EXTENSIONS)
        every { extFile.toFile() } returns bioFile
        every { extSerializationService.deserializeList(any()) } returns sequenceOf(extFile)
        every { serializationService.serializeFileList(any(), any(), any()) } answers {
            val sequence = arg<Sequence<BioFile>>(0)
            val format = arg<SubFormat>(1)
            val stream = arg<OutputStream>(2)
            stream.use { it.write("${sequence.toList().size}-$format".toByteArray()) }
        }

        val result = testInstance.serialize(extFileList, SubFormat.XML, target)

        assertThat(result).hasContent("1-XML")
    }
}
