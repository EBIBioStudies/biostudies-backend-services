package uk.ac.ebi.extended.serialization.deserializers

import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.NfsFile
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize
import java.io.FileNotFoundException

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun deserialize() {
        val file = tempFolder.createFile("test-file.txt")
        val json = jsonObj {
            "file" to file.absolutePath
            "fileName" to "test-file.txt"
            "path" to "test-file.txt"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                }
            )
            "extType" to "nfsFile"
        }.toString()

        val extFile = testInstance.deserialize<ExtFile>(json) as NfsFile
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `deserialize with inner path`() {
        val file = tempFolder.createFile("test-file.txt")
        val json = jsonObj {
            "file" to file.absolutePath
            "fileName" to "test-file.txt"
            "path" to "a/b/test-file.txt"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                }
            )
            "extType" to "nfsFile"
        }.toString()

        val extFile = testInstance.deserialize<ExtFile>(json) as NfsFile
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.fileName).isEqualTo("a/b/test-file.txt")
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `invalid file`() {
        val json = jsonObj {
            "file" to "/i/dont/exist/file.txt"
            "fileName" to "file.txt"
            "extType" to "nfsFile"
        }.toString()

        assertThrows<FileNotFoundException> { testInstance.deserialize<ExtFile>(json) }
    }
}
