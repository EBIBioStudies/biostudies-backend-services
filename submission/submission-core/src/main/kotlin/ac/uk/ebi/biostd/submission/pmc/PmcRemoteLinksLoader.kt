package ac.uk.ebi.biostd.submission.pmc

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.submission.domain.submitter.ExecutionArg
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import uk.ac.ebi.biostd.client.cluster.model.Job

class PmcRemoteLinksLoader(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
) {
    suspend fun loadLinks(config: ProcessConfig): Job =
        remoteSubmitterExecutor.executeRemotely(
            buildList {
                add(ExecutionArg("config.limit", config.limit))
                config.chunkSize?.let { ExecutionArg("config.chunkSize", it) }
                config.waitSeconds?.let { ExecutionArg("config.waitSeconds", it) }
            },
            Mode.LOAD_PMC_LINKS,
        )
}
