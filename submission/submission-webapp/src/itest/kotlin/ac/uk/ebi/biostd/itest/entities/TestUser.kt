package ac.uk.ebi.biostd.itest.entities

import ac.uk.ebi.biostd.common.properties.StorageMode
import ebi.ac.uk.api.security.RegisterRequest

interface TestUser {
    val username: String
    val email: String
    val password: String
    val superUser: Boolean
    val storageMode: StorageMode

    fun asRegisterRequest(): RegisterRequest = RegisterRequest(username, email, password, storageMode = storageMode.name)
}
