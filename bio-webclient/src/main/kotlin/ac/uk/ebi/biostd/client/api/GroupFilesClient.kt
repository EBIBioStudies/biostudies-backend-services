package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.GroupFilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.util.web.normalize
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import java.io.File

private const val GROUP_FILES_URL = "/files/groups"
private const val GROUP_FOLDER_URL = "/folder/groups"

internal class GroupFilesClient(private val template: RestTemplate) : GroupFilesOperations {

    override fun listGroupFiles(groupName: String, relativePath: String): List<UserFile> {
        return template.getForObject<Array<UserFile>>(
            "$GROUP_FILES_URL/$groupName${normalize(relativePath)}").orEmpty().toList()
    }

    override fun uploadGroupFiles(groupName: String, files: List<File>, relativePath: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        template.postForEntity(
            "$GROUP_FILES_URL/$groupName${normalize(relativePath)}", HttpEntity(body, headers), Void::class.java)
    }

    override fun createGroupFolder(groupName: String, folderName: String, relativePath: String) {
        template.postForObject<Unit>("$GROUP_FOLDER_URL/$groupName${normalize(relativePath)}?folder=$folderName")
    }

    override fun deleteGroupFile(groupName: String, fileName: String, relativePath: String) {
        template.delete("$GROUP_FILES_URL/$groupName${normalize(relativePath)}?fileName=$fileName")
    }
}
