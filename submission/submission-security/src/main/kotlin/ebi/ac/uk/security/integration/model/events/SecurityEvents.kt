package ebi.ac.uk.security.integration.model.events

import ac.uk.ebi.biostd.persistence.model.DbUser

sealed class SecurityEvents

class UserRegister(val user: DbUser, val activationLink: String) : SecurityEvents()
class UserActivated(val user: DbUser) : SecurityEvents()
class PasswordReset(val user: DbUser, val activationLink: String) : SecurityEvents()
