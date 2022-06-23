package uk.ac.ebi.fire.client.integration.web

import mu.KotlinLogging
import org.springframework.retry.support.RetryTemplate
import uk.ac.ebi.fire.client.model.FileType
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

private val logger = KotlinLogging.logger {}

/**
 * Wrap function execution function into kotlin runCatching to allow descriptive error reporting.
 *
 * @param opt the function description.
 * @param func the function to be performed.
 */
private fun <T> RetryTemplate.execute(opt: String, func: () -> T): T {
    logger.debug(opt) { "Executing operation: $opt" }
    return execute<T, Exception> {
        runCatching { func() }
            .onFailure { error -> logger.error(error) { "Fail to perform operation: $opt, ${it.retryCount + 1}" } }
            .getOrThrow()
    }
}

@Suppress("TooManyFunctions")
internal class RetryWebClient(
    private val fireClient: FireClient,
    private val template: RetryTemplate,
) : FireClient {
    override fun save(file: File, md5: String): FireApiFile {
        val opt = "Save file ${file.name}, md5=$md5"
        return template.execute(opt) { fireClient.save(file, md5) }
    }

    override fun setPath(fireOid: String, path: String) {
        val opt = "Set path fireOid='$fireOid', path=$path"
        template.execute(opt) { fireClient.setPath(fireOid, path) }
    }

    override fun unsetPath(fireOid: String) {
        val opt = "Unset path fireOid='$fireOid'"
        template.execute(opt) { fireClient.unsetPath(fireOid) }
    }

    override fun setBioMetadata(fireOid: String, accNo: String?, fileType: FileType?, published: Boolean?) {
        val opt = "Metadata update fireOid='$fireOid', accNo='$accNo', fileType='$fileType', published='$published'"
        template.execute(opt) { fireClient.setBioMetadata(fireOid, accNo, fileType, published) }
    }

    override fun downloadByPath(path: String): File? {
        val opt = "Download file path='$path'"
        return template.execute(opt) { fireClient.downloadByPath(path) }
    }

    override fun downloadByFireId(fireOid: String, fileName: String): File {
        val opt = "Download file fireOid='$fireOid', fileName='$fileName'"
        return template.execute(opt) { fireClient.downloadByFireId(fireOid, fileName) }
    }

    override fun findByMd5(md5: String): List<FireApiFile> {
        val opt = "Find file md5='$md5'"
        return template.execute(opt) { fireClient.findByMd5(md5) }
    }

    override fun findByAccNo(accNo: String): List<FireApiFile> {
        val opt = "Find file by accNo='$accNo'"
        return template.execute(opt) { fireClient.findByAccNo(accNo) }
    }

    override fun findByAccNoAndPublished(accNo: String, published: Boolean): List<FireApiFile> {
        val opt = "Find file by accNo='$accNo'"
        return template.execute(opt) { fireClient.findByAccNoAndPublished(accNo, published) }
    }

    override fun findByPath(path: String): FireApiFile? {
        val opt = "Find file by path='$path'"
        return template.execute(opt) { fireClient.findByPath(path) }
    }

    override fun findAllInPath(path: String): List<FireApiFile> {
        val opt = "Find files by path='$path'"
        return template.execute(opt) { fireClient.findAllInPath(path) }
    }

    override fun publish(fireOid: String) {
        val opt = "Publish file fireOid='$fireOid'"
        template.execute(opt) { fireClient.publish(fireOid) }
    }

    override fun unpublish(fireOid: String) {
        val opt = "Unpublish file fireOid='$fireOid'"
        template.execute(opt) { fireClient.unpublish(fireOid) }
    }
}
