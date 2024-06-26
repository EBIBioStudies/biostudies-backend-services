package uk.ac.ebi.extended.serialization.deserializers

import com.fasterxml.jackson.module.kotlin.readValue
import ebi.ac.uk.dsl.json.jsonArray
import ebi.ac.uk.dsl.json.jsonObj
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import ebi.ac.uk.util.collections.second
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import uk.ac.ebi.extended.serialization.service.ExtSerializationService

@ExtendWith(TemporaryFolderExtension::class)
class ExtFileDeserializerTest(private val tempFolder: TemporaryFolder) {
    private val testInstance = ExtSerializationService.mapper

    @Test
    fun `deserialize ext file when nfs file`() {
        val file = tempFolder.createFile("nfs-file.txt")
        val json =
            jsonObj {
                "fileName" to "nfs-file.txt"
                "filePath" to "folder/nfs-file.txt"
                "relPath" to "Files/folder/nfs-file.txt"
                "fullPath" to file.absolutePath
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Data"
                        },
                        jsonObj {
                            "name" to "Source"
                            "value" to null
                            "reference" to true
                        },
                    )
                "extType" to "nfsFile"
                "type" to "file"
                "size" to file.size()
                "md5" to file.md5()
            }.toString()

        val extFile = testInstance.readValue<ExtFile>(json) as NfsFile

        assertThat(extFile.fileName).isEqualTo("nfs-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/nfs-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/nfs-file.txt")
        assertThat(extFile.fullPath).isEqualTo(file.absolutePath)
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.attributes).hasSize(2)

        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
        assertThat(extFile.attributes.first().reference).isEqualTo(false)

        assertThat(extFile.attributes.second().name).isEqualTo("Source")
        assertThat(extFile.attributes.second().value).isNull()
        assertThat(extFile.attributes.second().reference).isEqualTo(true)
    }

    @Test
    fun `deserialize ext file when fire file`() {
        val json =
            jsonObj {
                "fileName" to "test-file.txt"
                "filePath" to "folder/test-file.txt"
                "relPath" to "Files/folder/test-file.txt"
                "fireId" to "fireId"
                "firePath" to "firePath"
                "published" to false
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Data"
                        },
                    )
                "type" to "file"
                "size" to 10
                "md5" to "fireFileMd5"
                "extType" to "fireFile"
            }.toString()

        val extFile = testInstance.readValue<ExtFile>(json) as FireFile

        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.fireId).isEqualTo("fireId")
        assertThat(extFile.published).isEqualTo(false)
        assertThat(extFile.firePath).isEqualTo("firePath")
        assertThat(extFile.md5).isEqualTo("fireFileMd5")
        assertThat(extFile.size).isEqualTo(10)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.type).isEqualTo(FILE)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }

    @Test
    fun `deserialize ext file when fire directory`() {
        val json =
            jsonObj {
                "fileName" to "test-file.txt"
                "filePath" to "folder/test-file.txt"
                "fireId" to "dirFireId"
                "firePath" to "dirFirePath"
                "published" to false
                "relPath" to "Files/folder/test-file.txt"
                "attributes" to
                    jsonArray(
                        jsonObj {
                            "name" to "Type"
                            "value" to "Data"
                        },
                    )
                "extType" to "fireFile"
                "type" to "directory"
                "size" to 20
                "md5" to "fireDirectoryMd5"
            }.toString()

        val extFile = testInstance.readValue<ExtFile>(json) as FireFile

        assertThat(extFile.fileName).isEqualTo("test-file.txt")
        assertThat(extFile.filePath).isEqualTo("folder/test-file.txt")
        assertThat(extFile.relPath).isEqualTo("Files/folder/test-file.txt")
        assertThat(extFile.fireId).isEqualTo("dirFireId")
        assertThat(extFile.firePath).isEqualTo("dirFirePath")
        assertThat(extFile.published).isFalse()
        assertThat(extFile.md5).isEqualTo("fireDirectoryMd5")
        assertThat(extFile.size).isEqualTo(20)
        assertThat(extFile.attributes).hasSize(1)
        assertThat(extFile.type).isEqualTo(DIR)
        assertThat(extFile.attributes.first().name).isEqualTo("Type")
        assertThat(extFile.attributes.first().value).isEqualTo("Data")
    }
}
