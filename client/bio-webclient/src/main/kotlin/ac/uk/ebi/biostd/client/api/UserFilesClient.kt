package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.delete
import ebi.ac.uk.commons.http.ext.getForObject
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.util.web.normalize
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.io.File
import java.nio.file.Files

private const val USER_FILES_URL = "/files/user"
private const val USER_FOLDER_URL = "/folder/user"

internal class UserFilesClient(
    private val client: WebClient,
) : FilesOperations {
    override fun downloadFile(
        fileName: String,
        relativePath: String,
    ): File {
        val tempFile = Files.createTempFile("biostudies-$fileName", ".tmp")
        val downloadUrl = "$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName"
        val response =
            client.method(GET)
                .uri(downloadUrl)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .retrieve()
                .bodyToMono<DataBuffer>()

        DataBufferUtils.write(response, tempFile).block()

        return tempFile.toFile()
    }

    override fun listUserFiles(relativePath: String): List<UserFile> {
        return client.getForObject<Array<UserFile>>("$USER_FILES_URL${normalize(relativePath)}").toList()
    }

    override fun uploadFiles(
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        client.post("$USER_FILES_URL${normalize(relativePath)}", RequestParams(headers, body))
    }

    override fun uploadFile(
        file: File,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { add("files", FileSystemResource(file)) }
        client.post("$USER_FILES_URL${normalize(relativePath)}", RequestParams(headers, body))
    }

    override fun createFolder(
        folderName: String,
        relativePath: String,
    ) {
        client.post("$USER_FOLDER_URL${normalize(relativePath)}?folder=$folderName")
    }

    override fun deleteFile(
        fileName: String,
        relativePath: String,
    ) {
        client.delete("$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName")
    }
}
