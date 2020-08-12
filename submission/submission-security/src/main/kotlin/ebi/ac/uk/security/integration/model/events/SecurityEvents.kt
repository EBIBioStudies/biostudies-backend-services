package ebi.ac.uk.security.integration.model.events

import ac.uk.ebi.biostd.persistence.model.DbUser

sealed class SecurityEvents

// TODO check whether this is necessary
class UserActivated(val user: DbUser) : SecurityEvents()
