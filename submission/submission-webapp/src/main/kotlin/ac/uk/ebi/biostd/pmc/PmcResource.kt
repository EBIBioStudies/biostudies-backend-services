package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.converters.BioUser
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor
import ac.uk.ebi.biostd.submission.pmc.PmcRemoteLinksLoader
import ac.uk.ebi.biostd.submission.pmc.ProcessConfig
import ac.uk.ebi.biostd.submission.pmc.ProcessingResult
import ebi.ac.uk.security.integration.model.api.SecurityUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.ac.ebi.biostd.client.cluster.model.Job

@RestController
@RequestMapping("/pmc")
@Tag(name = "PMC Links", description = "Internal processing endpoints for Europe PMC link loading.")
class PmcResource(
    private val pmcLinksProcessor: PmcLinksProcessor,
    private val pmcRemoteLinksLoader: PmcRemoteLinksLoader,
) {
    @Operation(summary = "Load PMC links for selected submissions")
    @PostMapping("/loadLinks")
    suspend fun loadLinks(
        @RequestBody request: LoadRequest,
        @BioUser user: SecurityUser,
    ): List<ProcessingResult> = pmcLinksProcessor.loadSubmissions(request.config, user, request.accNos)

    @Operation(summary = "Load PMC links from database-selected submissions")
    @PostMapping("/loadDbLinks")
    suspend fun loadDbLinks(
        @RequestBody request: LoadFromDbRequest,
        @BioUser user: SecurityUser,
    ): List<ProcessingResult> = pmcLinksProcessor.loadFromDb(request.config, user)

    @Operation(summary = "Run PMC link loading on the cluster")
    @PostMapping("/loadLinksRemotly")
    suspend fun loadLinksRemotly(
        @RequestBody request: LoadFromDbRequest,
    ): Job = pmcRemoteLinksLoader.loadLinks(request.config)
}

data class LoadRequest(
    val accNos: List<String>,
    val config: ProcessConfig,
)

data class LoadFromDbRequest(
    val config: ProcessConfig,
)
