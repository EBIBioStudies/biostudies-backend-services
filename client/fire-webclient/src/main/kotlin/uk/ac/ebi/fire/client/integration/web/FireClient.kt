package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

@Suppress("TooManyFunctions")
interface FireClient {
    fun save(file: File, md5: String, size: Long): FireApiFile

    fun setPath(fireOid: String, path: String)

    fun unsetPath(fireOid: String)

    fun downloadByPath(path: String): File?

    fun downloadByFireId(fireOid: String, fileName: String): File

    fun findByMd5(md5: String): List<FireApiFile>

    fun findByPath(path: String): FireApiFile?

    fun findAllInPath(path: String): List<FireApiFile>

    fun publish(fireOid: String)

    fun unpublish(fireOid: String)

    fun downloadByMd5(md5: String): File? =
        findByMd5(md5).firstOrNull()?.let { downloadByFireId(it.fireOid, it.objectMd5) }
}
