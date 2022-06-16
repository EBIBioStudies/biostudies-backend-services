package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ebi.ac.uk.model.Collection
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/projects", "/collections"])
@PreAuthorize("isAuthenticated()")
class CollectionResource(private val collectionService: CollectionService) {
    @GetMapping
    @ResponseBody
    fun getUserCollections(
        @BioUser user: SecurityUser
    ): List<Collection> = collectionService.getAllowedCollections(user, ATTACH)
}
