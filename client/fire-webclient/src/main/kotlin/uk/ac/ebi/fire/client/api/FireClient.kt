package uk.ac.ebi.fire.client.api

import ebi.ac.uk.io.ext.size
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import org.springframework.web.client.RestTemplate
import uk.ac.ebi.fire.client.integration.web.FireOperations
import uk.ac.ebi.fire.client.model.FireFile
import java.io.File
import java.nio.file.Files

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"
internal const val FIRE_OBJECTS_URL = "/fire/objects"

internal class FireClient(
    private val tmpDirPath: String,
    private val template: RestTemplate
) : FireOperations {
    override fun save(file: File, md5: String): FireFile {
        val headers = HttpHeaders().apply {
            set(FIRE_MD5_HEADER, md5)
            set(FIRE_SIZE_HEADER, file.size().toString())
        }

        val formData = listOf(
            FIRE_FILE_PARAM to FileSystemResource(file),
        )
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))

        return template.postForObject(FIRE_OBJECTS_URL, HttpEntity(body, headers))
    }

    override fun setPath(fireOid: String, path: String) {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        return template.put("$FIRE_OBJECTS_URL/$fireOid/firePath", HttpEntity(null, headers))
    }

    override fun downloadByPath(path: String): File {
        val tmpFile = File(tmpDirPath, path.substringAfterLast("/"))
        val fileContent = template.getForObject<ByteArray>("$FIRE_OBJECTS_URL/blob/path/$path")
        Files.write(tmpFile.toPath(), fileContent)

        return tmpFile
    }

    override fun publish(fireOid: String) {
        template.put("$FIRE_OBJECTS_URL/$fireOid/publish", null)
    }

    override fun unpublish(fireOid: String) {
        template.delete("$FIRE_OBJECTS_URL/$fireOid/publish")
    }
}
