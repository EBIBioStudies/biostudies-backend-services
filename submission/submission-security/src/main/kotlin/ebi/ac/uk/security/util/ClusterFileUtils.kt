package ebi.ac.uk.security.util

import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.api.ClusterOperations
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions

private val logger = KotlinLogging.logger {}

// TODO this might not be needed since we won't be executing single file commands in the cluster but executing the jar there instead
// TODO add defaultMode to application properties
// TODO all file related permissions could use this class
// TODO tests
enum class FilePermissions(val unixCode: Int, val posixPermissions: Set<PosixFilePermission>) {
    RWX__X___(710, PosixFilePermissions.fromString("rwx--x---")),
    RWXRWX___(770, PosixFilePermissions.fromString("rwxrwx---")),
}

// TODO this and all file operations should be moved to a new package called commons-files
class ClusterFileUtils(
    private val clusterClient: ClusterOperations
) {
    suspend fun createFolder(
        path: Path,
        permissions: FilePermissions,
    ) {
        val job = JobSpec(
            queue = DataMoverQueue,
            command = "mkdir -m ${permissions.unixCode} -p $path"
        )

        logger.info { "Started creating the cluster folder $path" }
        clusterClient.awaitJob(job)
        logger.info { "Finished creating the cluster folder $path" }
    }
}
