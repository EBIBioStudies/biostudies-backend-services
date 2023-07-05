package uk.ac.ebi.fire.client.api

import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.delete
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.postForObject
import ebi.ac.uk.commons.http.ext.put
import ebi.ac.uk.commons.http.ext.retrieveBlocking
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestOperations
import org.springframework.web.reactive.function.client.WebClient
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"

@Suppress("TooManyFunctions")
internal class FireWebClient(
    private val client: WebClient
) : FireWebClient {
    override fun save(file: File, md5: String, size: Long): FireApiFile {
        val headers = HttpHeaders().apply {
            set(FIRE_MD5_HEADER, md5)
            set(FIRE_SIZE_HEADER, size.toString())
        }

        val body = LinkedMultiValueMap<String, Any>().apply {
            add(FIRE_FILE_PARAM, FileSystemResource(file))
        }

        return client.postForObject("/objects", RequestParams(headers, body))
    }

    override fun setPath(fireOid: String, path: String) {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        client.put("/objects/$fireOid/firePath", RequestParams(headers))
    }

    override fun unsetPath(fireOid: String) {
        client.delete("/objects/$fireOid/firePath")
    }

    override fun findByMd5(md5: String): List<FireApiFile> {
        return client.getForObject<Array<FireApiFile>>("/objects/md5/$md5").toList()
    }

    override fun findByPath(path: String): FireApiFile? {
        return client.getOrNull("/objects/path/$path")
    }

    override fun findAllInPath(path: String): List<FireApiFile> {
        return client.getOrNull<Array<FireApiFile>>("/objects/entries/path/$path").orEmpty().toList()
    }

    override fun publish(fireOid: String): FireApiFile {
        return client.putForObject<FireApiFile>("/objects/$fireOid/publish")
    }

    override fun unpublish(fireOid: String) {
        client.delete("/objects/$fireOid/publish")
    }

    override fun delete(fireOid: String) {
        client.delete("/objects/$fireOid")
    }
}

/**
 * Perform same as @see [RestOperations.getForObject] but maps 404 status response into null result.
 */
private inline fun <reified T> WebClient.getOrNull(url: String): T? {
    val result = runCatching {
        getForObject<T>(url)
    }

    return result.getOrElse { if (it is HttpClientErrorException && it.statusCode == NOT_FOUND) null else throw it }
}

// TODO these methods will be moved to a separate common package in future PRs
internal inline fun <reified T> WebClient.putForObject(url: String, params: RequestParams? = null): T {
    return put().retrieveBlocking<T>(url, params)!!
}
