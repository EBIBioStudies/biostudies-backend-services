package ac.uk.ebi.biostd.itest.common

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser
import java.io.File

val enableFire
    get() = System.getProperty("enableFire").toBoolean()

val mongoMode = System.getProperty("itest.mode") == "mongo"

internal fun getWebClient(serverPort: Int, user: TestUser): BioWebClient {
    val securityClient = SecurityWebClient.create("http://localhost:$serverPort")
    return securityClient.getAuthenticatedClient(user.email, user.password)
}

internal fun createUser(testUser: TestUser, serverPort: Int) {
    SecurityWebClient.create("http://localhost:$serverPort").registerUser(testUser.asRegisterRequest())
}

fun File.clean() {
    listFiles()?.forEach {
        if (it.isFile) {
            it.delete()
        } else {
            if (it.name in remainingDirectories) it.cleanDirectory() else it.deleteRecursively()
        }
    }
}

private val remainingDirectories = setOf("submission", "request-files", "dropbox", "magic", "tmp")

private fun File.cleanDirectory(): File {
    listFiles()?.forEach { it.deleteRecursively() }
    return this
}
