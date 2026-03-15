package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.PmcRemoteLinksLoader
import ac.uk.ebi.biostd.submission.pmc.ProcessingResult
import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.model.Job

@RestController
@RequestMapping("/pmc")
class PmcResource(
    private val pmcLinksProcessor: PmcLinksProcessor,
    private val pmcRemoteLinksLoader: PmcRemoteLinksLoader,
) {
    @PostMapping("/loadLinks")
    suspend fun loadLinks(
        @RequestBody request: LoadRequest,
        @BioUser user: SecurityUser,
    ): List<ProcessingResult> = pmcLinksProcessor.loadSubmissions(user, request.accNos)

    @PostMapping("/loadDbLinks")
    suspend fun loadDbLinks(
        @RequestBody request: LoadFromDbRequest,
        @BioUser user: SecurityUser,
    ): List<ProcessingResult> = pmcLinksProcessor.loadFromDb(user, request.limit)

    @PostMapping("/loadLinksRemotly")
    suspend fun loadLinksRemotly(
        @RequestBody request: LoadFromDbRequest,
    ): Job = pmcRemoteLinksLoader.loadLinks(request.limit)
}

data class LoadRequest(
    val accNos: List<String>,
)

data class LoadFromDbRequest(
    val limit: Int,
)
