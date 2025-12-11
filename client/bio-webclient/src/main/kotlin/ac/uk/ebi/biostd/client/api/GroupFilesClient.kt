package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.MultipartBuilder
import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObjectAsync
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.model.DirFilePath
import ebi.ac.uk.model.GroupPath
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
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
        val tempFile = KFiles.createTempFile(fileName)
        val response =
            client
                .post()
                .uri("$GROUP_FILES_URL/$groupName/download")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(BodyInserters.fromValue(DirFilePath(relativePath, fileName)))
                .retrieve()
                .bodyToFlux<DataBuffer>()
        DataBufferUtils.write(response, tempFile).awaitFirstOrNull()

        return tempFile.toFile()
    }

    override suspend fun listGroupFiles(
        groupName: String,
        relativePath: String,
    ): List<UserFile> {
        val body = GroupPath(relativePath)
        return client.postForObjectAsync("$GROUP_FILES_URL/$groupName/query", RequestParams(body = body))
    }

    override suspend fun uploadGroupFiles(
        groupName: String,
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body =
            MultipartBuilder()
                .add("filePath", relativePath)
                .addAll("files", files.map { FileSystemResource(it) })
                .build()

        client.postForObjectAsync<Unit>("$GROUP_FILES_URL/$groupName/upload", RequestParams(headers, body))
    }

    override suspend fun createGroupFolder(
        groupName: String,
        folderName: String,
        relativePath: String,
    ) {
        val body = DirFilePath(relativePath, folderName)
        client.postForObjectAsync<Unit>("$GROUP_FOLDER_URL/$groupName/create", RequestParams(body = body))
    }

    override suspend fun deleteGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String,
    ) {
        val body = DirFilePath(relativePath, fileName)
        client.postForObjectAsync<Unit>("$GROUP_FILES_URL/$groupName/delete", RequestParams(body = body))
    }
}
