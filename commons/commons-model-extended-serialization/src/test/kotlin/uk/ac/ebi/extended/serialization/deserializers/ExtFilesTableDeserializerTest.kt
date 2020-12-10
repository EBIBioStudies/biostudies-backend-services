package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFileTable
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize

@ExtendWith(TemporaryFolderExtension::class)
class ExtFilesTableDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val file = tempFolder.createFile("test-file.txt")
        val json = jsonObj {
            "files" to jsonArray(jsonObj {
                "file" to file.absolutePath
                "fileName" to "test-file.txt"
                "path" to "test-file.txt"
                "attributes" to jsonArray(jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                })
                "extType" to "file"
            })
            "extType" to "filesTable"
        }.toString()

        val extFilesTable = testInstance.deserialize<ExtFileTable>(json)
        assertThat(extFilesTable.files).hasSize(1)

        val extFile = extFilesTable.files.first()
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }
}
