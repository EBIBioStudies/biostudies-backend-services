package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireFile
import java.io.File

interface FireOperations {
    fun save(file: File, md5: String): FireFile

    fun setPath(fireOid: String, path: String)

    fun downloadByPath(path: String): File

    fun publish(fireOid: String)

    fun unpublish(fireOid: String)
}
