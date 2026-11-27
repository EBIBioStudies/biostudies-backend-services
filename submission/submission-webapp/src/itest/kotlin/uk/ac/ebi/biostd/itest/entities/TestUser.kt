package uk.ac.ebi.biostd.itest.entities

import uk.ac.ebi.biostd.api.security.RegisterRequest
import uk.ac.ebi.biostd.common.properties.StorageMode

interface TestUser {
    val username: String
    val email: String
    val password: String
    val superUser: Boolean
    val storageMode: StorageMode

    fun asRegisterRequest(): RegisterRequest =
        RegisterRequest(
            username,
            email,
            password,
            path = "/activate",
            storageMode = storageMode.name,
            instanceKey = "http://localhost:8080",
        )
}
