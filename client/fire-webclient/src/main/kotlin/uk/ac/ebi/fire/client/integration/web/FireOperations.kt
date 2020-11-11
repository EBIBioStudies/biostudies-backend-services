package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireFile
import uk.ac.ebi.fire.client.model.MetadataEntry
import java.io.File

interface FireOperations {
    fun findByPath(path: String): FireFile?
    fun findByMetadata(vararg metadata: MetadataEntry): List<FireFile>
    fun downloadByPath(path: String): File
    fun save(file: File, path: String, md5: String, vararg metadata: MetadataEntry): FireFile
    fun move(source: String, target: String)
    fun delete(fireOid: String)
    fun publish(fireOid: String)
    fun unpublish(fireOid: String)
}
