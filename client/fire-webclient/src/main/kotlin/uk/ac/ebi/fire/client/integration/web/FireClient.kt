package uk.ac.ebi.fire.client.integration.web

import uk.ac.ebi.fire.client.model.FireApiFile
import java.io.File

interface FireClient : FireWebClient, FireS3Client

@Suppress("TooManyFunctions")
interface FireWebClient {
    suspend fun save(
        file: File,
        md5: String,
        size: Long,
    ): FireApiFile

    suspend fun setPath(
        fireOid: String,
        path: String,
    ): FireApiFile

    suspend fun unsetPath(fireOid: String)

    suspend fun findByPath(path: String): FireApiFile?

    suspend fun findAllInPath(path: String): List<FireApiFile>

    suspend fun publish(fireOid: String): FireApiFile

    suspend fun unpublish(fireOid: String)

    suspend fun delete(fireOid: String)
}

interface FireS3Client {
    fun downloadByPath(path: String): File?
}
