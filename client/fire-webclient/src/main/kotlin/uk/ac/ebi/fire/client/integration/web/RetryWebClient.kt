package uk.ac.ebi.fire.client.integration.web

import org.springframework.retry.support.RetryTemplate
import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

@Suppress("TooManyFunctions")
internal class RetryWebClient(
    private val fireClient: FireClient,
    private val retryTemplate: RetryTemplate
) : FireClient {
    override fun save(file: File, md5: String): FireApiFile =
        retryTemplate.execute<FireApiFile, Exception> { fireClient.save(file, md5) }

    override fun setPath(fireOid: String, path: String): Unit =
        retryTemplate.execute<Unit, Exception> { fireClient.setPath(fireOid, path) }

    override fun unsetPath(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fireClient.unsetPath(fireOid) }

    override fun setBioMetadata(fireOid: String, accNo: String?, fileType: String?, published: Boolean?): Unit =
        retryTemplate.execute<Unit, Exception> { fireClient.setBioMetadata(fireOid, accNo, fileType, published) }

    override fun downloadByPath(path: String): File? =
        retryTemplate.execute<File?, Exception> { fireClient.downloadByPath(path) }

    override fun downloadByFireId(fireOid: String, fileName: String): File =
        retryTemplate.execute<File, Exception> { fireClient.downloadByFireId(fireOid, fileName) }

    override fun findByMd5(md5: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fireClient.findByMd5(md5) }

    override fun findByAccNo(accNo: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fireClient.findByAccNo(accNo) }

    override fun findByAccNoAndPublished(accNo: String, published: Boolean): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fireClient.findByAccNoAndPublished(accNo, published) }

    override fun findByPath(path: String): FireApiFile? =
        retryTemplate.execute<FireApiFile?, Exception> { fireClient.findByPath(path) }

    override fun findAllInPath(path: String): List<FireApiFile> =
        retryTemplate.execute<List<FireApiFile>, Exception> { fireClient.findAllInPath(path) }

    override fun publish(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fireClient.publish(fireOid) }

    override fun unpublish(fireOid: String): Unit =
        retryTemplate.execute<Unit, Exception> { fireClient.unpublish(fireOid) }
}
