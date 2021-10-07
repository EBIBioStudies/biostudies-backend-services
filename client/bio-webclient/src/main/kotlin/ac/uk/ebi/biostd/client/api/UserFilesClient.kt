package ac.uk.ebi.biostd.client.api

import ac.uk.ebi.biostd.client.integration.web.FilesOperations
import ebi.ac.uk.api.UserFile
import ebi.ac.uk.commons.http.spring.saveInTempFile
import ebi.ac.uk.util.web.normalize
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RequestCallback
import org.springframework.web.client.ResponseExtractor
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import org.springframework.web.client.postForObject
import java.io.File

private const val USER_FILES_URL = "/files/user"
private const val USER_FOLDER_URL = "/folder/user"

internal class UserFilesClient(private val template: RestTemplate) : FilesOperations {
    override fun downloadFile(fileName: String, relativePath: String): File {
        val requestCallback = RequestCallback { it.headers.accept = listOf(APPLICATION_OCTET_STREAM) }
        val responseExtractor = ResponseExtractor { it.saveInTempFile("biostudies-$fileName") }
        val downloadUrl = "$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName"
        return template.execute(downloadUrl, GET, requestCallback, responseExtractor)!!
    }

    override fun listUserFiles(relativePath: String): List<UserFile> {
        return template.getForObject<Array<UserFile>>("$USER_FILES_URL${normalize(relativePath)}").orEmpty().toList()
    }

    override fun uploadFiles(files: List<File>, relativePath: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { files.forEach { add("files", FileSystemResource(it)) } }
        template.postForEntity("$USER_FILES_URL${normalize(relativePath)}", HttpEntity(body, headers), Void::class.java)
    }

    override fun uploadFile(file: File, relativePath: String) {
        val headers = HttpHeaders().apply { contentType = MediaType.MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply { add("files", FileSystemResource(file)) }
        template.postForEntity("$USER_FILES_URL${normalize(relativePath)}", HttpEntity(body, headers), Void::class.java)
    }

    override fun createFolder(folderName: String, relativePath: String) {
        template.postForObject<Unit>("$USER_FOLDER_URL${normalize(relativePath)}?folder=$folderName")
    }

    override fun deleteFile(fileName: String, relativePath: String) {
        template.delete("$USER_FILES_URL${normalize(relativePath)}?fileName=$fileName")
    }
}
