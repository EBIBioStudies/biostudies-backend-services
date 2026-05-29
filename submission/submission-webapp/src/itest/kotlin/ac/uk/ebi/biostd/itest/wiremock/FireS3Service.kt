package ac.uk.ebi.biostd.itest.wiremock

import kotlinx.coroutines.runBlocking
import uk.ac.ebi.fire.client.integration.web.FireS3Client
import java.io.File

class FireS3Service(
    private val fireS3Client: FireS3Client,
) {
    fun upload(
        file: File,
        path: String,
    ) {
        runBlocking { fireS3Client.upload(file, path) }
    }

    fun deleteFile(path: String) {
        runBlocking { fireS3Client.deleteFile(path) }
    }
}
