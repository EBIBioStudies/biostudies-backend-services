package uk.ac.ebi.fire.client.api

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.GetObjectRequest
import com.amazonaws.services.s3.model.S3Object
import uk.ac.ebi.fire.client.integration.web.FireS3Client
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class S3Client(
    private val amazonS3Client: AmazonS3,
    private val bucketName: String,
) : FireS3Client {
    override fun downloadByPath(path: String): File? {
        val stream = getFireObjectByPath(path).objectContent
        val tmp = File.createTempFile(DEFAULT_PREFIX, path.substringAfterLast("/"))
        Files.copy(stream, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return tmp
    }

    private fun getFireObjectByPath(path: String?): S3Object {
        val getObjectRequest = GetObjectRequest(bucketName, path)
        return amazonS3Client.getObject(getObjectRequest)
    }

    companion object {
        const val DEFAULT_PREFIX = "tmp_s3_file"
    }
}
