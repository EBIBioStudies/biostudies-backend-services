package ac.uk.ebi.biostd.submission.domain.cleanup

import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.CLEAN_UP
import ac.uk.ebi.biostd.submission.domain.cleanup.ExtUserSpaceCleanUpService.CleanUpMode.NOTIFY
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor

class ExtUserSpaceCleanUpService(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
    private val localUserSpaceCleanUpService: LocalUserSpaceCleanUpService,
) {
    suspend fun cleanUp(
        mode: CleanUpMode,
        remote: Boolean,
    ) {
        suspend fun notify() {
            if (remote.not()) {
                localUserSpaceCleanUpService.sendNotifications()
            } else {
                remoteSubmitterExecutor.executeRemotely(emptyList(), Mode.NOTIFY_USER_SPACE_CLEAN_UP)
            }
        }

        when (mode) {
            NOTIFY -> notify()
            CLEAN_UP -> TODO()
        }
    }

    enum class CleanUpMode {
        NOTIFY,
        CLEAN_UP,
    }
}
