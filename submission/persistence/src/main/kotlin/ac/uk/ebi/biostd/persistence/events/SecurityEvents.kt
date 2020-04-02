package ac.uk.ebi.biostd.persistence.events

import ac.uk.ebi.biostd.persistence.model.DbUser

sealed class SecurityEvents

data class UserRegisterEvent(val user: DbUser, val activationLink: String?) : SecurityEvents()
data class UserActivatedEvent(val user: DbUser) : SecurityEvents()
