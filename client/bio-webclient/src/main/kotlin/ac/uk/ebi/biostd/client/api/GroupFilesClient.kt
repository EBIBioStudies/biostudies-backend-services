package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.MultipartBuilder
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.util.web.normalize
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBodilessEntity
import org.springframework.web.reactive.function.client.awaitBody
import org.springframework.web.reactive.function.client.bodyToFlux
import java.io.File

private const val GROUP_FILES_URL = "/files/groups"
private const val GROUP_FOLDER_URL = "/folder/groups"

internal class GroupFilesClient(
    private val client: WebClient,
) : GroupFilesOperations {
    override suspend fun downloadGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String,
    ): File {
        val tempFile = KFiles.createTempFile("biostudies-$groupName-$fileName", ".tmp")
        val downloadUrl = "$GROUP_FILES_URL/$groupName${normalize(relativePath)}?fileName=$fileName"
        val response =
            client
                .get()
                .uri(downloadUrl)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .retrieve()
                .bodyToFlux<DataBuffer>()
        DataBufferUtils.write(response, tempFile).awaitFirstOrNull()
        return tempFile.toFile()
    }

    override suspend fun listGroupFiles(
        groupName: String,
        relativePath: String,
    ): List<UserFile> =
        client
            .get()
            .uri("$GROUP_FILES_URL/$groupName${normalize(relativePath)}")
            .retrieve()
            .awaitBody()

    override suspend fun uploadGroupFiles(
        groupName: String,
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = MultipartBuilder().addAll("files", files.map { FileSystemResource(it) }).build()
        client
            .post()
            .uri("$GROUP_FILES_URL/$groupName${normalize(relativePath)}")
            .headers { it.addAll(headers) }
            .body(BodyInserters.fromMultipartData(body))
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun createGroupFolder(
        groupName: String,
        folderName: String,
        relativePath: String,
    ) {
        client
            .post()
            .uri("$GROUP_FOLDER_URL/$groupName${normalize(relativePath)}?folder=$folderName")
            .retrieve()
            .awaitBodilessEntity()
    }

    override suspend fun deleteGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String,
    ) {
        client
            .delete()
            .uri("$GROUP_FILES_URL/$groupName${normalize(relativePath)}?fileName=$fileName")
            .retrieve()
            .awaitBodilessEntity()
    }
}
