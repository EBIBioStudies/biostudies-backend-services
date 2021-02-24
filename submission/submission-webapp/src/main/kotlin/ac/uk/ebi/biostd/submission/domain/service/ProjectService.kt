package ac.uk.ebi.biostd.submission.domain.service

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ebi.ac.uk.model.Collection
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class ProjectService(
    private val collectionSqlDataService: CollectionDataService,
    private val userPrivilegesService: IUserPrivilegesService
) {
    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Collection> {
        val accessTags = userPrivilegesService.allowedProjects(user.email, accessType)
        return collectionSqlDataService.findProjectsByAccessTags(accessTags).map { Collection(it.accNo, it.title) }
    }
}
