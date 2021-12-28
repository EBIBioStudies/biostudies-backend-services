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
    fun `serialize FileList`() {
        val fileSystem = temporaryFolder.createFile("serialization.xml")
        val files = (1..50000).map { File("folder$it/file.txt", size = it.toLong(), attributes = attributes(it)) }
        val iterator = files.iterator()

        testInstance.serializeFileList(files, fileSystem)

        testInstance.deserializeFileList(fileSystem).forEach { file ->
            assertThat(file).isEqualToComparingFieldByField(iterator.next())
        }
    }

    private fun attributes(numberFile: Int) = listOf(
        Attribute(name = "name1$numberFile", value = "value1$numberFile"),
        Attribute(name = "name2$numberFile", value = "value2$numberFile"),
        Attribute(name = "name3$numberFile", value = "value3$numberFile")
    )
}
