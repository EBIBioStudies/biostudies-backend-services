package uk.ac.ebi.fire.client.integration.web

import org.springframework.retry.support.RetryTemplate
import uk.ac.ebi.fire.client.model.FireApiFile
import uk.ac.ebi.fire.client.retry.execute
import java.io.File

@Suppress("TooManyFunctions")
internal class RetryWebClient(
    private val fireClient: FireClient,
    private val template: RetryTemplate,
) : FireClient {
    override fun save(file: File, md5: String, size: Long): FireApiFile {
        val opt = "Save file ${file.name}, md5=$md5, size=$size"
        return template.execute(opt) { fireClient.save(file, md5, size) }
    }

    override fun setPath(fireOid: String, path: String) {
        val opt = "Set path fireOid='$fireOid', path=$path"
        template.execute(opt) { fireClient.setPath(fireOid, path) }
    }

    override fun unsetPath(fireOid: String) {
        val opt = "Unset path fireOid='$fireOid'"
        template.execute(opt) { fireClient.unsetPath(fireOid) }
    }

    override fun downloadByPath(path: String): File {
        val opt = "Download file path='$path'"
        return template.execute(opt) { fireClient.downloadByPath(path) }
    }

    override fun findByMd5(md5: String): List<FireApiFile> {
        val opt = "Find file md5='$md5'"
        return template.execute(opt) { fireClient.findByMd5(md5) }
    }

    override fun findByPath(path: String): FireApiFile? {
        val opt = "Find file by path='$path'"
        return template.execute(opt) { fireClient.findByPath(path) }
    }

    override fun findAllInPath(path: String): List<FireApiFile> {
        val opt = "Find files by path='$path'"
        return template.execute(opt) { fireClient.findAllInPath(path) }
    }

    override fun publish(fireOid: String): FireApiFile {
        val opt = "Publish file fireOid='$fireOid'"
        return template.execute(opt) { fireClient.publish(fireOid) }
    }

    override fun unpublish(fireOid: String) {
        val opt = "Unpublish file fireOid='$fireOid'"
        template.execute(opt) { fireClient.unpublish(fireOid) }
    }

    override fun delete(fireOid: String) {
        val opt = "Delete file fireOid='$fireOid'"
        template.execute(opt) { fireClient.delete(fireOid) }
    }
}
