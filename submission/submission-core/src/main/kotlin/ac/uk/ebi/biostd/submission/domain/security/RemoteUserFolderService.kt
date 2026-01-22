package ac.uk.ebi.biostd.submission.domain.security

import ac.uk.ebi.biostd.common.properties.MIGRATION_EMAIL
import ac.uk.ebi.biostd.common.properties.MIGRATION_OPTIONS
import ac.uk.ebi.biostd.common.properties.Mode
import ac.uk.ebi.biostd.submission.domain.submitter.ExecutionArg
import ac.uk.ebi.biostd.submission.domain.submitter.RemoteSubmitterExecutor
import ebi.ac.uk.model.MigrateHomeOptions

class RemoteUserFolderService(
    private val remoteSubmitterExecutor: RemoteSubmitterExecutor,
) {
    suspend fun updateMagicFolder(
        email: String,
        options: MigrateHomeOptions,
    ) {
        val args =
            listOf<ExecutionArg>(
                ExecutionArg(MIGRATION_EMAIL, email),
                ExecutionArg("${MIGRATION_OPTIONS}.storageMode", options.storageMode),
                ExecutionArg("${MIGRATION_OPTIONS}.copyFilesSinceDays", options.copyFilesSinceDays.toString()),
                ExecutionArg("${MIGRATION_OPTIONS}.onlyIfEmptyFolder", options.onlyIfEmptyFolder.toString()),
            )
        remoteSubmitterExecutor.executeRemotely(args, Mode.MIGRAGE_USER_FOLDER)
    }
}
