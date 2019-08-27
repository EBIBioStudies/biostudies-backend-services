package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.util.web.normalize
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

private const val GROUP_FILES_URL = "/files/groups"
private const val GROUP_FOLDER_URL = "/folder/groups"

internal class GroupFilesClient(private val template: RestTemplate) : GroupFilesOperations {
    override fun downloadGroupFile(groupName: String, fileName: String, relativePath: String): File {
        val requestCallback = RequestCallback { it.headers.accept = listOf(MediaType.APPLICATION_OCTET_STREAM) }
        val responseExtractor = ResponseExtractor { saveFile(it, groupName, fileName) }
        val downloadUrl = "${groupFileUrl(groupName, relativePath)}?fileName=$fileName"
        return template.execute(downloadUrl, HttpMethod.GET, requestCallback, responseExtractor)
    }

    override fun listGroupFiles(groupName: String, relativePath: String): List<UserFile> {
        return template.getForObject<Array<UserFile>>(groupFileUrl(groupName, relativePath)).orEmpty().toList()
    }

    override fun uploadGroupFiles(groupName: String, files: List<File>, relativePath: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        template.postForEntity(groupFileUrl(groupName, relativePath), HttpEntity(body, headers), Void::class.java)
    }

    override fun createGroupFolder(groupName: String, folderName: String, relativePath: String) {
        template.postForObject<Unit>("${groupFolderUrl(groupName, relativePath)}?folder=$folderName")
    }

    override fun deleteGroupFile(groupName: String, fileName: String, relativePath: String) {
        template.delete("${groupFileUrl(groupName, relativePath)}?fileName=$fileName")
    }

    private fun groupFileUrl(groupName: String, relativePath: String) =
        "$GROUP_FILES_URL/$groupName${normalize(relativePath)}"

    private fun groupFolderUrl(groupName: String, relativePath: String) =
        "$GROUP_FOLDER_URL/$groupName${normalize(relativePath)}"

    private fun saveFile(response: ClientHttpResponse, group: String, fileName: String): File {
        val targetPath = Files.createTempFile("biostudies-$group-$fileName", ".tmp")
        Files.copy(response.body, targetPath, StandardCopyOption.REPLACE_EXISTING); return targetPath.toFile()
    }
}
