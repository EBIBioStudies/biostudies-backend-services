package ebi.ac.uk.security.integration.model.events

import ac.uk.ebi.biostd.persistence.model.User

sealed class SecurityEvents

class UserPreRegister(val user: User, val activationLink: String) : SecurityEvents()
class UserRegister(val user: User) : SecurityEvents()
class PasswordReset(val user: User, val activationLink: String) : SecurityEvents()
