package ac.uk.ebi.biostd.persistence.events

import ac.uk.ebi.biostd.persistence.model.User

sealed class SecurityEvents

data class UserRegisterEvent(val user: User, val activationLink: String?) : SecurityEvents()
data class UserActivatedEvent(val user: User) : SecurityEvents()
