package uk.ac.ebi.fire.client.integration.web

import kotlinx.coroutines.runBlocking
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.fire.client.retry.SuspendRetryTemplate
import java.io.File

@Suppress("TooManyFunctions")
internal class RetryWebClient(
    private val fireClient: FireWebClient,
    private val fireS3Client: FireS3Client,
    private val template: SuspendRetryTemplate,
) : FireClient {
    override suspend fun save(
        file: File,
        md5: String,
        size: Long,
    ): FireApiFile {
        val opt = "Save file ${file.name}, md5=$md5, size=$size"
        return template.execute(opt) { fireClient.save(file, md5, size) }
    }

    override suspend fun setPath(
        fireOid: String,
        path: String,
    ): FireApiFile {
        val opt = "Set path fireOid='$fireOid', path=$path"
        return template.execute(opt) { fireClient.setPath(fireOid, path) }
    }

    override suspend fun unsetPath(fireOid: String) {
        val opt = "Unset path fireOid='$fireOid'"
        template.execute(opt) { fireClient.unsetPath(fireOid) }
    }

    override suspend fun findByPath(path: String): FireApiFile? {
        val opt = "Find file by path='$path'"
        return template.execute(opt) { fireClient.findByPath(path) }
    }

    override suspend fun findAllInPath(path: String): List<FireApiFile> {
        val opt = "Find files by path='$path'"
        return template.execute(opt) { fireClient.findAllInPath(path) }
    }

    override suspend fun publish(fireOid: String): FireApiFile {
        val opt = "Publish file fireOid='$fireOid'"
        return template.execute(opt) { fireClient.publish(fireOid) }
    }

    override suspend fun unpublish(fireOid: String): FireApiFile {
        val opt = "Unpublish file fireOid='$fireOid'"
        return template.execute(opt) { fireClient.unpublish(fireOid) }
    }

    override suspend fun delete(fireOid: String) {
        val opt = "Delete file fireOid='$fireOid'"
        template.execute(opt) { fireClient.delete(fireOid) }
    }

    override suspend fun downloadByPath(path: String): File? {
        val opt = "Download file path='$path'"
        return runBlocking { template.execute(opt) { fireS3Client.downloadByPath(path) } }
    }
}
