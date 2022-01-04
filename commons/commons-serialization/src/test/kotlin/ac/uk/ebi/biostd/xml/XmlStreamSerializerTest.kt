package ac.uk.ebi.biostd.xml

import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
internal class XmlStreamSerializerTest(
    private val temporaryFolder: TemporaryFolder
) {
    val testInstance = XmlStreamSerializer()

    @Test
    fun `serialize - deserialize FileList`() {
        val fileSystem = temporaryFolder.createFile("serialization.xml")
        val files = (1..20000).map { File("folder$it/file.txt", size = it.toLong(), attributes = attributes(it)) }
        val iterator = files.iterator()

        fileSystem.outputStream().use { testInstance.serializeFileList(files.asSequence(), it) }

        fileSystem.inputStream().use {
            testInstance.deserializeFileList(it).forEach { file ->
                assertThat(file).isEqualToComparingFieldByField(iterator.next())
            }
        }
    }

    private fun attributes(numberFile: Int) =
        (1..3).map { Attribute(name = "name$it-file$numberFile", value = "value$it-file$numberFile") }
}
