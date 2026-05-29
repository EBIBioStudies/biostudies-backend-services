package uk.ac.ebi.fire.client.api

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.asByteStream
import aws.smithy.kotlin.runtime.content.writeToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uk.ac.ebi.fire.client.integration.web.FireS3Client
import java.io.File

class S3KClient(
    private val bucketName: String,
    private val s3Client: S3Client,
) : FireS3Client {
    override suspend fun downloadByPath(path: String): File {
        val tmp =
            withContext(Dispatchers.IO) {
                File.createTempFile(DEFAULT_PREFIX, path.substringAfterLast("/"))
            }
        copyFile(path, tmp)
        return tmp
    }

    override suspend fun upload(
        file: File,
        path: String,
    ) {
        val request =
            PutObjectRequest {
                bucket = bucketName
                key = path
                body = file.asByteStream()
            }
        s3Client.putObject(request)
    }

    override suspend fun deleteFile(path: String) {
        val request =
            DeleteObjectRequest {
                bucket = bucketName
                key = path
            }
        s3Client.deleteObject(request)
    }

    private suspend fun copyFile(
        path: String,
        file: File,
    ) {
        val request =
            GetObjectRequest {
                key = path
                bucket = bucketName
            }
        return s3Client.getObject(request) { resp ->
            resp.body?.writeToFile(file)
        }
    }

    companion object {
        const val DEFAULT_PREFIX = "tmp_s3_file"
    }
}
