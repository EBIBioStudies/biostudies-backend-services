package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.common.MultipartBuilder
import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ac.uk.ebi.biostd.client.integration.web.UserOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.postForObjectAsync
import ebi.ac.uk.extended.model.ExtUser
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.model.DirFilePath
import ebi.ac.uk.model.FilePath
import ebi.ac.uk.model.FolderStats
import ebi.ac.uk.model.MigrateHomeOptions
import ebi.ac.uk.model.RenameFilePath
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

private const val USER_FILES_URL = "/files/user"
private const val USER_FOLDER_URL = "/folder/user"

internal class UserOperationsClient(
    private val client: WebClient,
) : UserOperations {
    override suspend fun getExtUser(email: String): ExtUser =
        client
            .get()
            .uri("/security/users/extended/$email")
            .retrieve()
            .awaitBody<ExtUser>()

    override suspend fun getUserHomeStats(email: String): FolderStats =
        client
            .get()
            .uri("/security/users/$email/home-stats")
            .retrieve()
            .awaitBody<FolderStats>()

    override suspend fun migrateUser(
        email: String,
        options: MigrateHomeOptions,
    ) {
        client
            .post()
            .uri("/security/users/$email/migrate")
            .body(BodyInserters.fromValue(options))
            .retrieve()
            .awaitBodilessEntity()
    }
}

internal class UserFilesClient(
    private val client: WebClient,
) : FilesOperations {
    override suspend fun downloadFile(
        fileName: String,
        relativePath: String,
    ): File {
        val tempFile = KFiles.createTempFile(fileName)
        val response =
            client
                .post()
                .uri("$USER_FILES_URL/download")
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .body(BodyInserters.fromValue(DirFilePath(relativePath, fileName)))
                .retrieve()
                .bodyToFlux<DataBuffer>()
        DataBufferUtils.write(response, tempFile).awaitFirstOrNull()

        return tempFile.toFile()
    }

    override suspend fun listUserFiles(relativePath: String): List<UserFile> {
        val body = FilePath(relativePath)
        return client.postForObjectAsync("$USER_FILES_URL/query", RequestParams(body = body))
    }

    override suspend fun uploadFiles(
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body =
            MultipartBuilder()
                .add("filePath", relativePath)
                .addAll("files", files.map { FileSystemResource(it) })
                .build()

        client.postForObjectAsync<Unit>("$USER_FILES_URL/upload", RequestParams(headers, body))
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
        val body = DirFilePath(relativePath, folderName)
        client.postForObjectAsync<Unit>("$USER_FOLDER_URL/create", RequestParams(body = body))
    }

    override suspend fun deleteFile(
        fileName: String,
        relativePath: String,
    ) {
        val body = DirFilePath(relativePath, fileName)
        client.postForObjectAsync<Unit>("$USER_FILES_URL/delete", RequestParams(body = body))
    }

    override suspend fun renameFile(relativePath: String, originalName: String, newName: String): Boolean {
        val body = RenameFilePath(relativePath, originalName, newName)
        return client.postForObjectAsync<Boolean>("$USER_FILES_URL/rename", RequestParams(body = body))
    }
}
