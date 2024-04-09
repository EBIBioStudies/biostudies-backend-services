package ebi.ac.uk.security.integration.components

import ac.uk.ebi.biostd.persistence.model.DbUserGroup

interface IGroupService {
    fun createGroup(
        groupName: String,
        description: String,
    ): DbUserGroup

    fun addUserInGroup(
        groupName: String,
        userEmail: String,
    )
}
