package uk.ac.ebi.biostd.client.cli.services

import com.github.ajalt.clikt.output.TermUi.echo
import ebi.ac.uk.api.security.UserProfile
import uk.ac.ebi.biostd.client.cli.config.DataMoverConfig
import uk.ac.ebi.biostd.client.cluster.api.ClusterClient
import uk.ac.ebi.biostd.client.cluster.api.SlurmClusterClient
import uk.ac.ebi.biostd.client.cluster.model.DataMoverQueue
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.io.File
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class DataMoverUserFilesService(
    private val config: DataMoverConfig = DataMoverConfig.load(),
    private val cluster: ClusterClient =
        SlurmClusterClient.create(config.sshKey.toString(), config.sshServer, config.logsPath, config.wrapperPath),
) {
    suspend fun upload(
        profile: UserProfile,
        source: File,
        relPath: String,
    ) {
        val root = config.userFilesRoots[profile.uploadType] ?: error("No userFilesRoots.${profile.uploadType} configured")
        val target = root.resolve(profile.secret).resolve(relPath)
        val jobSpec =
            JobSpec(
                queue = DataMoverQueue,
                minutes = config.jobMinutes,
                command = transferCommand(source, target, config.group),
            )

        echo("Started transferring files through the datamover")
        val job =
            cluster.triggerJobSync(
                jobSpec = jobSpec,
                checkJobInterval = POLL_INTERVAL,
                maxDuration = config.jobMinutes.minutes,
            )
        echo("Finished transferring files. Datamover job ${job.id} completed. Logs: ${job.logsPath}")
    }

    companion object {
        private val POLL_INTERVAL = 5.seconds

        internal fun transferCommand(
            source: File,
            target: Path,
            group: String,
        ): String =
            """
            set -euo pipefail
            umask 007
            source=${source.absoluteFile.normalize().path.shellQuoted()}
            target=${target.toString().shellQuoted()}
            group=${group.shellQuoted()}
            mkdir -p -- "${'$'}target"
            chgrp -- "${'$'}group" "${'$'}target"
            find -L "${'$'}source" -type f -print0 | while IFS= read -r -d '' file; do
                name="${'$'}{file##*/}"
                rsync --copy-links --times --perms --chmod=Fu=rw,Fg=rw,Fo= --ignore-times \
                    --partial-dir=.rsync-partial --no-whole-file --progress --stats -- "${'$'}file" "${'$'}target/"
                chgrp -- "${'$'}group" "${'$'}target/${'$'}name"
            done
            """.trimIndent()

        private fun String.shellQuoted() = "'${replace("'", "'\"'\"'")}'"
    }
}
