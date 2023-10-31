package ac.uk.ebi.biostd.json.deserialization

import ac.uk.ebi.biostd.json.JsonSerializer
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.serialization.extensions.deserializeAsFlow

@ExtendWith(TemporaryFolderExtension::class)
class FileListDeserializerTest(
    private val tempFolder: TemporaryFolder,
) {
    private val testInstance = JsonSerializer.mapper

    @Test
    fun `deserialize file list as flow`() = runTest {
        val json = jsonArray(
            jsonObj { "path" to "File1.txt" },
            jsonObj { "path" to "inner/folder" },
        ).toString()
        tempFolder.createFile("FileList.json", json).inputStream().use {
            val files = testInstance.deserializeAsFlow<BioFile>(it).toList()
            assertThat(files).hasSize(2)
            assertThat(files.first().path).isEqualTo("File1.txt")
            assertThat(files.second().path).isEqualTo("inner/folder")
        }
    }
}
