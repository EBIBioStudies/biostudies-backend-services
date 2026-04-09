package ac.uk.ebi.biostd.itest.wiremock

import com.amazonaws.services.s3.AmazonS3
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

class FireS3Service(
    private val s3Bucket: String,
    private val amazonS3: AmazonS3,
) {
    fun upload(
        file: File,
        path: String,
    ) {
        logger.info {
            "## Started Uploading file ${file.absolutePath} to path $path and bucket $s3Bucket ##"
        }
        logger.info { "Client config: $amazonS3" }
        amazonS3.putObject(s3Bucket, path, file)
        logger.info {
            "## Finished Uploading file ${file.absolutePath} to path $path and bucket $s3Bucket ## "
        }
    }

    fun deleteFile(path: String) {
        amazonS3.deleteObject(s3Bucket, path)
    }
}
