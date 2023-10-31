package ac.uk.ebi.biostd.xml

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.BioFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class XmlStreamSerializerTest(
    private val temporaryFolder: TemporaryFolder,
) {
    val testInstance = XmlStreamSerializer()

    @Test
    fun `serialize - deserialize FileList`() = runTest {
        val fileSystem = temporaryFolder.createFile("serialization.xml")
        val files = (1..20_000).map { BioFile("folder$it/file.txt", attributes = attributes(it)) }
        val iterator = files.iterator()

        fileSystem.outputStream().use { testInstance.serializeFileList(files.asFlow(), it) }
        val result = fileSystem.inputStream().use { testInstance.deserializeFileList(it).toList() }
        assertThat(result).allSatisfy() { assertThat(it).isEqualToComparingFieldByField(iterator.next()) }
        assertThat(result).hasSize(20_000)
    }

    private fun attributes(numberFile: Int) =
        (1..3).map { Attribute(name = "name$it-file$numberFile", value = "value$it-file$numberFile") }
}
