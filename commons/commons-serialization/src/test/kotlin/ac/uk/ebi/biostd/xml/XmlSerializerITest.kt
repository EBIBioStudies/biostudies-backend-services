package ac.uk.ebi.biostd.xml

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class XmlSerializerITest(
    private val temporaryFolder: TemporaryFolder
) {
    private val testInstance = XmlSerializer()
    private val submission = createVenousBloodMonocyte()

    @Test
    fun `serialize and deserialize sample submission`() {
        val xml = testInstance.serialize(submission)
        val result = testInstance.deserialize(xml)

        assertThat(result).isNotNull
        assertThat(result).isEqualTo(submission)
    }

    @Test
    fun `serialize - deserialize FileList`() {
        val fileSystem = temporaryFolder.createFile("serialization.xml")
        val files = (1..20000).map { File("folder$it/file.txt", size = it.toLong(), attributes = attributes(it)) }
        val iterator = files.iterator()

        testInstance.serializeFileList(files.asSequence(), fileSystem)

        testInstance.deserializeFileList(fileSystem).forEach { file ->
            assertThat(file).isEqualToComparingFieldByField(iterator.next())
        }
    }

    private fun attributes(numberFile: Int) =
        (1..3).map { Attribute(name = "name$it-file$numberFile", value = "value$it-file$numberFile") }
}
