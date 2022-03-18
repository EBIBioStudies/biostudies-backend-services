package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireFile
import java.io.File

@Suppress("TooManyFunctions")
interface FireOperations {
    fun save(file: File, md5: String): FireFile

    fun setPath(fireOid: String, path: String)

    fun unsetPath(fireOid: String)

    fun setBioMetadata(fireOid: String, accNo: String?, published: Boolean?)

    fun downloadByPath(path: String): File

    fun downloadByFireId(fireOid: String, fileName: String): File

    fun findByMd5(md5: String): List<FireFile>

    fun findByAccNo(accNo: String): List<FireFile>

    fun findByAccNoAndPublished(accNo: String, published: Boolean): List<FireFile>

    fun findAllInPath(path: String): List<FireFile>

    fun publish(fireOid: String)

    fun unpublish(fireOid: String)
}
