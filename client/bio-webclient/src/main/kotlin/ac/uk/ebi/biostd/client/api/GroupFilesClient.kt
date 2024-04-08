package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
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

private const val GROUP_FILES_URL = "/files/groups"
private const val GROUP_FOLDER_URL = "/folder/groups"

internal class GroupFilesClient(
    private val client: WebClient,
) : GroupFilesOperations {
    override fun downloadGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String,
    ): File {
        val tempFile = Files.createTempFile("biostudies-$groupName-$fileName", ".tmp")
        val downloadUrl = "${groupFileUrl(groupName, relativePath)}?fileName=$fileName"
        val response =
            client.method(GET)
                .uri(downloadUrl)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .retrieve()
                .bodyToMono<DataBuffer>()

        DataBufferUtils.write(response, tempFile).block()

        return tempFile.toFile()
    }

    override fun listGroupFiles(
        groupName: String,
        relativePath: String,
    ): List<UserFile> {
        return client.getForObject<Array<UserFile>>(groupFileUrl(groupName, relativePath)).toList()
    }

    override fun uploadGroupFiles(
        groupName: String,
        files: List<File>,
        relativePath: String,
    ) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        client.post(groupFileUrl(groupName, relativePath), RequestParams(headers, body))
    }

    override fun createGroupFolder(
        groupName: String,
        folderName: String,
        relativePath: String,
    ) {
        client.post("${groupFolderUrl(groupName, relativePath)}?folder=$folderName")
    }

    override fun deleteGroupFile(
        groupName: String,
        fileName: String,
        relativePath: String,
    ) {
        client.delete("${groupFileUrl(groupName, relativePath)}?fileName=$fileName")
    }

    private fun groupFileUrl(
        groupName: String,
        relativePath: String,
    ) = "$GROUP_FILES_URL/$groupName${normalize(relativePath)}"

    private fun groupFolderUrl(
        groupName: String,
        relativePath: String,
    ) = "$GROUP_FOLDER_URL/$groupName${normalize(relativePath)}"
}
