package ac.uk.ebi.biostd.itest.wiremock

import com.amazonaws.services.s3.AmazonS3
import java.io.File

class FireS3Service(
    private val s3Bucket: String,
    private val amazonS3: AmazonS3,
) {
    fun upload(file: File, path: String) {
        amazonS3.putObject(s3Bucket, "/$path", file)
    }

    fun deleteFile(path: String) {
        amazonS3.deleteObject(s3Bucket, "/$path")
    }
}
