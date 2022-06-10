package ac.uk.ebi.biostd.submission.domain.helpers

import ac.uk.ebi.biostd.persistence.common.model.AccessType
import ac.uk.ebi.biostd.persistence.common.service.CollectionDataService
import ebi.ac.uk.model.Collection
import ebi.ac.uk.security.integration.components.IUserPrivilegesService
import ebi.ac.uk.security.integration.model.api.SecurityUser

class CollectionService(
    private val collectionSqlDataService: CollectionDataService,
    private val userPrivilegesService: IUserPrivilegesService,
) {
    fun getAllowedProjects(user: SecurityUser, accessType: AccessType): List<Collection> {
        val accessTags = userPrivilegesService.allowedCollections(user.email, accessType)
        return collectionSqlDataService.findCollectionsByAccessTags(accessTags).map { Collection(it.accNo, it.title) }
    }
}
