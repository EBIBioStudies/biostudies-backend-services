package uk.ac.ebi.fire.client.api

import ebi.ac.uk.io.ext.size
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import uk.ac.ebi.fire.client.exception.FireClientException
import uk.ac.ebi.fire.client.integration.web.FireClient
import uk.ac.ebi.fire.client.model.FileType
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File
import java.nio.file.Files

internal const val FIRE_FILE_PARAM = "file"
internal const val FIRE_MD5_HEADER = "x-fire-md5"
internal const val FIRE_PATH_HEADER = "x-fire-path"
internal const val FIRE_SIZE_HEADER = "x-fire-size"
internal const val FIRE_BIO_PUBLISHED = "bio-published"

const val FIRE_BIO_ACC_NO = "bio-accNo"
const val FIRE_BIO_FILE_TYPE = "bio-file-type"
const val FIRE_OBJECTS_URL = "/objects"

@Suppress("TooManyFunctions")
internal class FireWebClient(
    private val tmpDirPath: String,
    private val template: RestTemplate,
) : FireClient {
    override fun save(file: File, md5: String): FireApiFile {
        val headers = HttpHeaders().apply {
            set(FIRE_MD5_HEADER, md5)
            set(FIRE_SIZE_HEADER, file.size().toString())
        }
        val formData = listOf(FIRE_FILE_PARAM to FileSystemResource(file))
        val body = LinkedMultiValueMap(formData.groupBy({ it.first }, { it.second }))
        return template.postForObject(FIRE_OBJECTS_URL, HttpEntity(body, headers))
    }

    override fun setPath(fireOid: String, path: String) {
        val headers = HttpHeaders().apply { set(FIRE_PATH_HEADER, path) }
        template.put("$FIRE_OBJECTS_URL/$fireOid/firePath", HttpEntity(null, headers))
    }

    override fun unsetPath(fireOid: String) {
        template.delete("$FIRE_OBJECTS_URL/$fireOid/firePath")
    }

    override fun setBioMetadata(fireOid: String, accNo: String?, fileType: FileType?, published: Boolean?) {
        val headers = HttpHeaders().apply { set(CONTENT_TYPE, APPLICATION_JSON_VALUE) }
        val body = buildList {
            accNo?.let { add("\"$FIRE_BIO_ACC_NO\": \"$it\"") }
            fileType?.let { add("\"$FIRE_BIO_FILE_TYPE\": \"${it.key}\"") }
            published?.let { add("\"$FIRE_BIO_PUBLISHED\": $published") }
        }.joinToString()

        template.put("$FIRE_OBJECTS_URL/$fireOid/metadata/set", HttpEntity("{ $body }", headers))
    }

    override fun downloadByPath(
        path: String,
    ): File? = findByPath(path)?.let {
        downloadFireFile(path.substringAfterLast("/"), "$FIRE_OBJECTS_URL/blob/path/$path")
    }

    override fun downloadByFireId(
        fireOid: String,
        fileName: String,
    ): File = downloadFireFile(fileName, "$FIRE_OBJECTS_URL/blob/$fireOid")

    private fun downloadFireFile(fileName: String, downloadUrl: String): File {
        val tmpFile = File(tmpDirPath, fileName)
        val fileContent = template.getForObject<ByteArray?>(downloadUrl)
        Files.write(tmpFile.toPath(), fileContent ?: byteArrayOf())
        return tmpFile
    }

    override fun findByMd5(md5: String): List<FireApiFile> =
        template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/md5/$md5").toList()

    override fun findByAccNo(accNo: String): List<FireApiFile> {
        val headers = HttpHeaders().apply { set(CONTENT_TYPE, APPLICATION_JSON_VALUE) }
        val body = "{ \"$FIRE_BIO_ACC_NO\": \"$accNo\" }"
        return template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", HttpEntity(body, headers))
            .toList()
    }

    override fun findByAccNoAndPublished(accNo: String, published: Boolean): List<FireApiFile> {
        val headers = HttpHeaders().apply { set(CONTENT_TYPE, APPLICATION_JSON_VALUE) }
        val body = "{ \"$FIRE_BIO_ACC_NO\": \"$accNo\", \"$FIRE_BIO_PUBLISHED\": $published }"
        return template.postForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/metadata", HttpEntity(body, headers))
            .toList()
    }

    override fun findByPath(path: String): FireApiFile? {
        return runCatching { template.getForObject<FireApiFile>("$FIRE_OBJECTS_URL/path/$path") }
            .getOrElse { if (it is FireClientException && it.statusCode == NOT_FOUND) return null else throw it }
    }

    override fun findAllInPath(path: String): List<FireApiFile> {
        return runCatching {
            template.getForObject<Array<FireApiFile>>("$FIRE_OBJECTS_URL/entries/path/$path").toList()
        }
            .getOrElse { if (it is FireClientException && it.statusCode == NOT_FOUND) return emptyList() else throw it }
    }

    override fun publish(fireOid: String) {
        template.put("$FIRE_OBJECTS_URL/$fireOid/publish", null)
    }

    override fun unpublish(fireOid: String) {
        template.delete("$FIRE_OBJECTS_URL/$fireOid/publish")
    }
}
