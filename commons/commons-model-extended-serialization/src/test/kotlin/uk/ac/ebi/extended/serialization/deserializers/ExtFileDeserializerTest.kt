package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.databind.JsonMappingException
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import uk.ac.ebi.serialization.extensions.deserialize

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `deserialize ext file when nfs file`() {
        val file = tempFolder.createFile("nfs-file.txt")
        val json = jsonObj {
            "fileName" to "nfs-file.txt"
            "filePath" to "folder/nfs-file.txt"
            "relPath" to "Files/folder/nfs-file.txt"
            "fullPath" to file.absolutePath
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                }
            )
            "extType" to "nfsFile"
            "type" to "file"
            "size" to file.size()
            "md5" to file.md5()
        }.toString()

        val extFile = testInstance.deserialize<ExtFile>(json) as NfsFile

        assertThat(extFile.fileName).isEqualTo("nfs-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/nfs-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/nfs-file.txt")
        assertThat(extFile.fullPath).isEqualTo(file.absolutePath)
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `deserialize ext file when fire file`() {
        val json = jsonObj {
            "fileName" to "test-file.txt"
            "filePath" to "folder/test-file.txt"
            "relPath" to "Files/folder/test-file.txt"
            "fireId" to "fireId"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                }
            )
            "type" to "file"
            "size" to 10
            "md5" to "fireFileMd5"
            "extType" to "fireFile"
        }.toString()

        val extFile = testInstance.deserialize<ExtFile>(json) as FireFile

        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.fireId).isEqualTo("fireId")
        assertThat(extFile.md5).isEqualTo("fireFileMd5")
        assertThat(extFile.size).isEqualTo(10)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `deserialize ext file when fire directory`() {
        val json = jsonObj {
            "fileName" to "test-file.txt"
            "filePath" to "folder/test-file.txt"
            "relPath" to "Files/folder/test-file.txt"
            "attributes" to jsonArray(
                jsonObj {
                    "name" to "Type"
                    "value" to "Data"
                }
            )
            "extType" to "fireDirectory"
            "type" to "directory"
            "size" to 20
            "md5" to "fireDirectoryMd5"
        }.toString()

        val extFile = testInstance.deserialize<ExtFile>(json) as FireDirectory

        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.md5).isEqualTo("fireDirectoryMd5")
        assertThat(extFile.size).isEqualTo(20)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `invalid file`() {
        val json = jsonObj {
            "fullPath" to "/i/dont/exist/file.txt"
            "fileName" to "file.txt"
            "extType" to "nfsFile"
        }.toString()

        assertThrows<JsonMappingException> { testInstance.deserialize<ExtFile>(json) }
    }
}
