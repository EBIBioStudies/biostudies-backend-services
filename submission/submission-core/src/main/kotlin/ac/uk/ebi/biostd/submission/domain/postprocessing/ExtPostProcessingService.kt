package ac.uk.ebi.biostd.submission.domain.postprocessing

import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_ALL
import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_INNER_FILES
import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_PAGETAB_FILES
import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_SINGLE
import ac.uk.ebi.biostd.common.properties.Mode.POST_PROCESS_STATS
import ac.uk.ebi.biostd.submission.domain.submitter.ExecutionArg
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor

class ExtPostProcessingService(
    private val localPostProcessingService: LocalPostProcessingService,
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
) {
    suspend fun postProcess(
        accNo: String,
        mode: PostProcessMode,
        remote: Boolean,
    ) {
        suspend fun postProcess() {
            if (remote.not()) {
                localPostProcessingService.postProcess(accNo)
            } else {
                remoteSubmitterExecutor.executeRemotely(asArgs(accNo), POST_PROCESS_SINGLE)
            }
        }

        suspend fun stats() {
            if (remote.not()) {
                localPostProcessingService.calculateStats(accNo)
            } else {
                remoteSubmitterExecutor.executeRemotely(asArgs(accNo), POST_PROCESS_STATS)
            }
        }

        suspend fun innerFiles() {
            if (remote.not()) {
                localPostProcessingService.indexSubmissionInnerFiles(accNo)
            } else {
                remoteSubmitterExecutor.executeRemotely(asArgs(accNo), POST_PROCESS_INNER_FILES)
            }
        }

        suspend fun pagetabFiles() {
            if (remote.not()) {
                localPostProcessingService.generateFallbackPageTabFiles(accNo)
            } else {
                remoteSubmitterExecutor.executeRemotely(asArgs(accNo), POST_PROCESS_PAGETAB_FILES)
            }
        }

        when (mode) {
            PostProcessMode.ALL -> postProcess()
            PostProcessMode.STATS -> stats()
            PostProcessMode.INNER_FILES -> innerFiles()
            PostProcessMode.PAGETAB -> pagetabFiles()
        }
    }

    suspend fun postProcessAll() {
        remoteSubmitterExecutor.executeRemotely(emptyList(), POST_PROCESS_ALL)
    }

    /**
     * Returns the accNo as command line args note that version is hardcoded as 0L since post-processing operations are
     * always performed over the latest version.
     */
    private fun asArgs(accNo: String): List<ExecutionArg> =
        buildList {
            add(ExecutionArg("submissions[0].accNo", accNo))
            add(ExecutionArg("submissions[0].version", 0L))
        }

    enum class PostProcessMode {
        ALL,
        STATS,
        INNER_FILES,
        PAGETAB,
    }
}
