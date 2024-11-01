package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.MultipartBuilder
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.util.web.normalize
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlux
import java.io.File

private const val USER_FILES_URL = "/files/user"
private const val USER_FOLDER_URL = "/folder/user"

internal class UserFilesClient(
    private val client: WebClient,
) : FilesOperations {
    override suspend fun downloadFile(
        fileName: String,
        relativePath: String,
    ): File {
        val tempFile = KFiles.createTempFile("biostudies-$fileName", ".tmp")
        val downloadUrl = "$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName"
        val response =
            client
                .method(GET)
                .uri(downloadUrl)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .retrieve()
                .bodyToFlux<DataBuffer>()
        DataBufferUtils.write(response, tempFile).awaitFirstOrNull()
        return tempFile.toFile()
    }

    override suspend fun listUserFiles(relativePath: String): List<UserFile> =
        client
            .get()
            .uri("$USER_FILES_URL${normalize(relativePath)}")
            .retrieve()
            .awaitBody()

    override suspend fun uploadFiles(
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = MultipartBuilder().addAll("files", files.map { FileSystemResource(it) }).build()
        client
            .post()
            .uri("$USER_FILES_URL${normalize(relativePath)}")
            .headers { it.addAll(headers) }
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun uploadFile(
        file: File,
        relativePath: String,
    ) {
        uploadFiles(listOf(file), relativePath)
    }

    override suspend fun createFolder(
        folderName: String,
        relativePath: String,
    ) {
        client
            .post()
            .uri("$USER_FOLDER_URL${normalize(relativePath)}?folder=$folderName")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun deleteFile(
        fileName: String,
        relativePath: String,
    ) {
        client
            .delete()
            .uri("$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName")
            .retrieve()
            .awaitBodilessEntity()
    }
}
