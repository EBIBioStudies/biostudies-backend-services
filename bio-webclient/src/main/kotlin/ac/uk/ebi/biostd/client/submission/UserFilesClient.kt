package ac.uk.ebi.biostd.client.submission

import ac.uk.ebi.biostd.client.integration.web.FilesOperations
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

private const val USER_FILES_URL = "/files/user"
private const val USER_FOLDER_URL = "/folder/user"

internal class UserFilesClient(private val template: RestTemplate) : FilesOperations {

    override fun listUserFiles(relativePath: String): List<UserFile> {
        return template.getForObject<Array<UserFile>>("$USER_FILES_URL${normalize(relativePath)}").orEmpty().toList()
    }

    override fun uploadFiles(files: List<File>, relativePath: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        template.postForEntity("$USER_FILES_URL${normalize(relativePath)}", HttpEntity(body, headers), Void::class.java)
    }

    override fun createFolder(folderName: String, relativePath: String) {
        template.postForObject<Unit>("$USER_FOLDER_URL${normalize(relativePath)}?folder=$folderName")
    }

    override fun deleteFile(fileName: String, relativePath: String) {
        template.delete("$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName")
    }
}
