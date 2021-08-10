package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireFile
import java.io.File

interface FireOperations {
    fun save(file: File, md5: String): FireFile

    fun setPath(fireOid: String, path: String)

    fun unsetPath(fireOid: String)

    fun downloadByPath(path: String): File

    fun findAllInPath(path: String): List<FireFile>

    fun publish(fireOid: String)

    fun unpublish(fireOid: String)
}
