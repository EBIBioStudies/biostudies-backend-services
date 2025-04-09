package uk.ac.ebi.fire.client.api

import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.deleteAsync
import ebi.ac.uk.commons.http.ext.getForObjectAsync
import ebi.ac.uk.commons.http.ext.postForObjectAsync
import ebi.ac.uk.commons.http.ext.putForObjectAsync
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestOperations
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.reactive.function.client.awaitBody
import uk.ac.ebi.fire.client.integration.web.FireWebClient
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"

@Suppress("TooManyFunctions")
internal class FireWebClient(
    private val client: WebClient,
) : FireWebClient {
    override suspend fun save(
        file: File,
        md5: String,
        size: Long,
    ): FireApiFile {
        val headers =
            HttpHeaders().apply {
                set(FIRE_MD5_HEADER, md5)
                set(FIRE_SIZE_HEADER, size.toString())
            }

        val body =
            LinkedMultiValueMap<String, Any>().apply {
                add(FIRE_FILE_PARAM, FileSystemResource(file))
            }

        return client.postForObjectAsync("/objects", RequestParams(headers, body))
    }

    override suspend fun setPath(
        fireOid: String,
        path: String,
    ): FireApiFile {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        return client.putForObjectAsync<FireApiFile>("/objects/$fireOid/firePath", RequestParams(headers))
    }

    override suspend fun unsetPath(fireOid: String) {
        client.deleteAsync("/objects/$fireOid/firePath")
    }

    override suspend fun findByPath(path: String): FireApiFile? = client.getOrNull("/objects/path/$path")

    override suspend fun findAllInPath(path: String): List<FireApiFile> =
        client.getOrNull<Array<FireApiFile>>("/objects/entries/path/$path").orEmpty().toList()

    override suspend fun publish(fireOid: String): FireApiFile = client.putForObjectAsync<FireApiFile>("/objects/$fireOid/publish")

    override suspend fun unpublish(fireOid: String): FireApiFile =
        client
            .delete()
            .uri("/objects/$fireOid/publish")
            .retrieve()
            .awaitBody()

    override suspend fun delete(fireOid: String) {
        client.deleteAsync("/objects/$fireOid")
    }
}

/**
 * Perform same as @see [RestOperations.getForObject] but maps 404 status response into null result.
 */
private suspend inline fun <reified T> WebClient.getOrNull(url: String): T? =
    runCatching {
        getForObjectAsync<T>(url)
    }.getOrElse { if (it is WebClientResponseException && it.statusCode == NOT_FOUND) null else throw it }
