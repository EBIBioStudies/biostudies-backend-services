package ac.uk.ebi.biostd.json

import ac.uk.ebi.biostd.test.createVenousBloodMonocyte
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Submission
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class JsonSerializerTest(
    private val temporaryFolder: TemporaryFolder
) {
    private val testInstance = JsonSerializer.mapper
    private val submission = createVenousBloodMonocyte()

    @Test
    fun `serialize and deserialize sample submission`() {
        val json = testInstance.writeValueAsString(submission)
        val deserialized = testInstance.readValue(json, Submission::class.java)

        assertThat(deserialized).isNotNull
        assertThat(deserialized).isEqualTo(submission)
    }

    @Test
    fun `serialize - deserialize FileList`() {
        val jsonSerializer = JsonSerializer()
        val fileSystem = temporaryFolder.createFile("serialization.json")
        val files = (1..20_000).map { File("folder$it/file.txt", size = 0L, attributes = attributes(it)) }
        val iterator = files.iterator()

        fileSystem.outputStream().use { jsonSerializer.serializeFileList(files.asSequence(), it) }

        fileSystem.inputStream().use {
            jsonSerializer.deserializeFileList(it).forEach { file ->
                assertThat(file).isEqualToComparingFieldByField(iterator.next())
            }
        }
    }

    private fun attributes(numberFile: Int) =
        (1..3).map { Attribute(name = "name$it-file$numberFile", value = "value$it-file$numberFile") }
}
