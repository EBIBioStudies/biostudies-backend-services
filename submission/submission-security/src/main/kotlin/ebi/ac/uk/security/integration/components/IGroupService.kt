package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.model.UserGroup

interface IGroupService {

    fun createGroup(groupName: String, description: String): UserGroup

    fun addUserInGroup(groupName: String, userEmail: String)
}
