package uk.ac.ebi.fire.client.api

import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.nio.file.Files

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"
private typealias ClientException = HttpClientErrorException

@Suppress("TooManyFunctions")
internal class FireWebClient(
    private val tmpDirPath: String,
    private val template: RestTemplate,
) : FireClient {
    override fun save(file: File, md5: String, size: Long): FireApiFile {
        val headers = HttpHeaders().apply {
            set(FIRE_MD5_HEADER, md5)
            set(FIRE_SIZE_HEADER, size.toString())
        }
        val formData = listOf(FIRE_FILE_PARAM to FileSystemResource(file))
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))
        return template.postForObject("/objects", HttpEntity(body, headers))
    }

    override fun setPath(fireOid: String, path: String) {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        template.put("/objects/$fireOid/firePath", HttpEntity(null, headers))
    }

    override fun unsetPath(fireOid: String) {
        template.delete("/objects/$fireOid/firePath")
    }

    override fun downloadByPath(
        path: String,
    ): File? {
        return downloadFireFile(path.substringAfterLast("/"), "/objects/blob/path/$path")
    }

    private fun downloadFireFile(fileName: String, downloadUrl: String): File? {
        return when (val fileContent = template.getForObjectOrNull<ByteArray?>(downloadUrl)) {
            null -> null
            else -> {
                val tmpFile = File(tmpDirPath, fileName)
                Files.write(tmpFile.toPath(), fileContent)
                tmpFile
            }
        }
    }

    override fun findByMd5(md5: String): List<FireApiFile> =
        template.getForObject<Array<FireApiFile>>("/objects/md5/$md5").toList()

    override fun findByPath(path: String): FireApiFile? = template.getForObjectOrNull("/objects/path/$path")

    override fun findAllInPath(path: String): List<FireApiFile> =
        template.getForObjectOrNull<Array<FireApiFile>>("/objects/entries/path/$path").orEmpty().toList()

    override fun publish(fireOid: String): FireApiFile {
        val response = template.exchange("/objects/$fireOid/publish", HttpMethod.PUT, null, FireApiFile::class.java)
        return requireNotNull(response.body) { "Expecting operation '/objects/$fireOid/publish' to retrieve an object" }
    }

    override fun unpublish(fireOid: String) {
        template.delete("/objects/$fireOid/publish")
    }

    override fun delete(fireOid: String) {
        template.delete("/objects/$fireOid")
    }
}

/**
 * Perform same as @see [RestOperations.getForObject] but maps 404 status response into null result.
 */
private inline fun <reified T> RestOperations.getForObjectOrNull(url: String, vararg uriVariables: Any): T? {
    val result = runCatching { getForObject<T>(url, *uriVariables) }
    return result.getOrElse { if (it is ClientException && it.statusCode == NOT_FOUND) null else throw it }
}
