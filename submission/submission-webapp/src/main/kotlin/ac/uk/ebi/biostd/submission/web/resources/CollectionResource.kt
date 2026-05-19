package ac.uk.ebi.biostd.submission.web.resources

import ac.uk.ebi.biostd.persistence.common.model.AccessType.ATTACH
import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.domain.helpers.CollectionService
import ebi.ac.uk.model.Collection
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(value = ["/projects", "/collections"])
@PreAuthorize("isAuthenticated()")
@Tag(name = "Collections", description = "Collections the authenticated user can submit to or attach files under.")
class CollectionResource(private val collectionService: CollectionService) {
    @GetMapping
    @ResponseBody
    @Operation(
        summary = "List Available Collections",
        description = "Return the collections where the authenticated user has permission to attach or submit content.",
    )
    suspend fun getUserCollections(
        @BioUser user: SecurityUser,
    ): List<Collection> = collectionService.getAllowedCollections(user, ATTACH)
}
