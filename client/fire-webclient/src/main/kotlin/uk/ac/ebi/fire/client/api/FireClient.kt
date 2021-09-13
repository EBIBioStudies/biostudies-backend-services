package uk.ac.ebi.fire.client.api

import ebi.ac.uk.io.ext.size
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import uk.ac.ebi.fire.client.integration.web.FireOperations
import uk.ac.ebi.fire.client.model.FireFile
import java.io.File
import java.nio.file.Files

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"

internal const val SUBMISSION_RELPATH_HEADER = "sub-relpath"

const val FIRE_OBJECTS_URL = "/fire/objects"

internal class FireClient(
    private val tmpDirPath: String,
    private val template: RestTemplate
) : FireOperations {
    override fun save(file: File, md5: String, relpath: String): FireFile {
        val headers = HttpHeaders().apply {
            set(FIRE_MD5_HEADER, md5)
            set(FIRE_SIZE_HEADER, file.size().toString())
            set(SUBMISSION_RELPATH_HEADER, relpath)
        }
        val formData = listOf(FIRE_FILE_PARAM to FileSystemResource(file))
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))
        return template.postForObject(FIRE_OBJECTS_URL, HttpEntity(body, headers))
    }

    override fun setPath(fireOid: String, path: String) {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        return template.put("$FIRE_OBJECTS_URL/$fireOid/firePath", HttpEntity(null, headers))
    }

    override fun unsetPath(fireOid: String) {
        template.delete("$FIRE_OBJECTS_URL/$fireOid/firePath")
    }

    override fun downloadByPath(
        path: String
    ): File = downloadFireFile(path.substringAfterLast("/"), "$FIRE_OBJECTS_URL/blob/path/$path")

    override fun downloadByFireId(
        fireOid: String,
        fileName: String
    ): File = downloadFireFile(fileName, "$FIRE_OBJECTS_URL/blob/$fireOid")

    private fun downloadFireFile(fileName: String, downloadUrl: String): File {
        val tmpFile = File(tmpDirPath, fileName)
        val fileContent = template.getForObject<ByteArray>(downloadUrl)
        Files.write(tmpFile.toPath(), fileContent)

        return tmpFile
    }

    override fun findAllInPath(path: String): List<FireFile> {
        runCatching {
            return template.getForObject("$FIRE_OBJECTS_URL/entries/path/$path")
        }

        return emptyList()
    }

    override fun publish(fireOid: String) {
        template.put("$FIRE_OBJECTS_URL/$fireOid/publish", null)
    }

    override fun unpublish(fireOid: String) {
        template.delete("$FIRE_OBJECTS_URL/$fireOid/publish")
    }
}
