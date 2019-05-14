package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.model.User
import ebi.ac.uk.api.security.LoginRequest
import ebi.ac.uk.api.security.RegisterRequest
import ebi.ac.uk.security.integration.model.api.UserInfo

interface ISecurityService {

    fun registerUser(request: RegisterRequest): User

    fun login(loginRequest: LoginRequest): UserInfo

    fun activate(activationKey: String)
}
