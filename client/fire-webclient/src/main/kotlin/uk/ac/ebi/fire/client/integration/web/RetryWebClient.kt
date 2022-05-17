package uk.ac.ebi.fire.client.integration.web

import org.springframework.retry.support.RetryTemplate
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

@Suppress("TooManyFunctions")
internal class RetryWebClient(
    private val fileOperations: FireClient,
    private val retryTemplate: RetryTemplate
) : FireClient {
    override fun save(file: File, md5: String): FireApiFile =
        retryTemplate.execute<FireApiFile, Exception> { fileOperations.save(file, md5) }

    override fun setPath(fireOid: String, path: String): Unit =
        retryTemplate.execute<Unit, Exception> { fileOperations.setPath(fireOid, path) }

    override fun unsetPath(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fileOperations.unsetPath(fireOid) }

    override fun setBioMetadata(fireOid: String, accNo: String?, fileType: String?, published: Boolean?): Unit =
        retryTemplate.execute<Unit, Exception> { fileOperations.setBioMetadata(fireOid, accNo, fileType, published) }

    override fun downloadByPath(path: String): File =
        retryTemplate.execute<File, Exception> { fileOperations.downloadByPath(path) }

    override fun downloadByFireId(fireOid: String, fileName: String): File =
        retryTemplate.execute<File, Exception> { fileOperations.downloadByFireId(fireOid, fileName) }

    override fun findByMd5(md5: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fileOperations.findByMd5(md5) }

    override fun findByAccNo(accNo: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fileOperations.findByAccNo(accNo) }

    override fun findByAccNoAndPublished(accNo: String, published: Boolean): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fileOperations.findByAccNoAndPublished(accNo, published) }

    override fun findByPath(path: String): FireApiFile? =
        retryTemplate.execute<FireApiFile?, Exception> { fileOperations.findByPath(path) }

    override fun findAllInPath(path: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fileOperations.findAllInPath(path) }

    override fun publish(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fileOperations.publish(fireOid) }

    override fun unpublish(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fileOperations.unpublish(fireOid) }
}
