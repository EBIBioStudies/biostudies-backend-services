package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.converters.BioUser
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/pmc")
class PmcResource(
    private val pmcLinksLoader: PmcLinksLoader,
) {
    @PostMapping("/loadLinks")
    suspend fun loadLinks(@RequestBody request: LoadRequest, @BioUser user: SecurityUser) {
        pmcLinksLoader.loadSubmission(user, request.accNo)
    }
}

data class LoadRequest(val accNo: String)