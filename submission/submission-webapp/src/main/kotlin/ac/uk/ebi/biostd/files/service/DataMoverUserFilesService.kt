package ac.uk.ebi.biostd.files.service

import ebi.ac.uk.security.integration.model.api.SecurityUser
import org.springframework.stereotype.Service
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.nio.file.Path

@Service
class DataMoverUserFilesService(
    private val clusterClient: ClusterClient,
) {
    suspend fun upload(
        user: SecurityUser,
        sourcePath: String,
        relativePath: String,
    ): String {
        val source = Path.of(sourcePath).normalize()
        require(source.isAbsolute) { "The cluster source path must be absolute: $sourcePath" }

        val userFolder = user.userFolder.path.normalize()
        val target = userFolder.resolveRelative(relativePath)
        val jobSpec =
            JobSpec(
                queue = DataMoverQueue,
                minutes = JOB_MINUTES,
                command = transferCommand(source, target, userFolder),
            )

        return clusterClient.triggerJobAsync(jobSpec).getOrThrow().id
    }

    companion object {
        private const val JOB_MINUTES = 2880 // 60 * 48 (48 hrs)

        /**
         * Builds a resumable command that copies the files under [source] into a flattened [target] directory.
         *
         * The command uses:
         * - `set -euo pipefail` to stop on command failures, unset variables, or failed pipeline stages.
         * - `umask 007` to grant access to the owner and group only.
         * - `source` as the cluster path to scan and `target` as the single destination directory.
         * - `user_folder` to obtain the user's group ID, which `chgrp` applies to the target and copied files.
         * - `mkdir -p --` to create the destination hierarchy; `--` prevents paths from being read as options.
         * - `find -L ... -type f -print0` to follow links and emit every file using null delimiters.
         * - `read -r -d ''` to safely consume file names containing spaces, quotes, or newlines.
         * - Shell base-name expansion to remove each file's parent path, flattening source subdirectories.
         * - `rsync --copy-links --times --perms` to copy link targets while preserving timestamps and permissions.
         * - `--chmod=Fu=rw,Fg=rw,Fo=` to make files writable by their owner and group and inaccessible to others.
         * - `--ignore-times` to always transfer the requested file even when size and timestamp match.
         * - `--partial-dir=.rsync-partial --no-whole-file` to retain partial data and resume through delta transfers.
         * - `--progress --stats` to write transfer progress and a summary to the cluster job log.
         *
         * @param source cluster-accessible file or directory to transfer
         * @param target destination directory inside the user's file space
         * @param userFolder user directory whose group ownership is applied to the transferred files
         */
        internal fun transferCommand(
            source: Path,
            target: Path,
            userFolder: Path,
        ): String =
            """
            set -euo pipefail
            umask 007
            source=${source.toString().shellQuoted()}
            target=${target.toString().shellQuoted()}
            user_folder=${userFolder.toString().shellQuoted()}
            group="${'$'}(ls -nd "${'$'}user_folder" | awk '{print ${'$'}4}')"
            mkdir -p -- "${'$'}target"
            chgrp "${'$'}group" "${'$'}target"
            find -L "${'$'}source" -type f -print0 | while IFS= read -r -d '' file; do
                name="${'$'}{file##*/}"
                rsync --copy-links --times --perms --chmod=Fu=rw,Fg=rw,Fo= --ignore-times \
                    --partial-dir=.rsync-partial --no-whole-file --progress --stats -- "${'$'}file" "${'$'}target/"
                chgrp "${'$'}group" "${'$'}target/${'$'}name"
            done
            """.trimIndent()

        private fun Path.resolveRelative(relativePath: String): Path {
            val requestedPath = Path.of(relativePath)
            require(requestedPath.isAbsolute.not()) { "The user file path must be relative: $relativePath" }

            val target = resolve(requestedPath).normalize()
            require(target.startsWith(this)) { "The user file path escapes the user folder: $relativePath" }
            return target
        }

        private fun String.shellQuoted() = "'${replace("'", "'\"'\"'")}'"
    }
}
