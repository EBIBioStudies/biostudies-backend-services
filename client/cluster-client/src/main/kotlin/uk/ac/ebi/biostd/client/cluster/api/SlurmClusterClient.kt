package uk.ac.ebi.biostd.client.cluster.api

import ebi.ac.uk.base.orFalse
import ebi.ac.uk.coroutines.waitUntil
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.common.JobSubmitFailException
import uk.ac.ebi.biostd.client.cluster.common.toSlurmJob
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.time.Duration.ofSeconds
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private val logger = KotlinLogging.logger {}

class SlurmClusterClient(
    private val wrapperPath: String,
    private val logsPath: String,
    private val sshServer: String,
    private val sshKey: String,
) : ClusterClient {
    private val sshClient by lazy { SshClient(sshMachine = sshServer, sshKey = sshKey) }

    override suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job> {
        val parameters = mutableListOf("sbatch --output=/dev/null")
        parameters.addAll(jobSpec.asParameter(wrapperPath, logsPath))
        val command = parameters.joinToString(separator = " ")
        logger.info { "Executing command '$command'" }

        return sshClient.runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response)
        }
    }

    override suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ): Job {
        val job = triggerJobAsync(jobSpec).getOrThrow()
        await(job, checkJobInterval, maxSecondsDuration)
        return job
    }

    /**
     * Return the slurm job status. PENDING/RUNNING/COMPLETING/COMPLETED
     */
    override suspend fun jobStatus(jobId: String): String {
        fun CommandRunner.runningJobStatus(): String? {
            val status = executeCommand("squeue --noheader --format=%T --job $jobId").second
            return status.ifBlank { null }
        }

        fun CommandRunner.historicalJobStatus(): String? {
            val command = "sacct --noheader --format=JobID,State --jobs=$jobId | grep \"^$jobId \" | awk '{print \$2}'"
            val status = executeCommand(command).second
            return status.ifBlank { null }
        }

        return sshClient.runInSession {
            val status =
                runningJobStatus()
                    ?: historicalJobStatus()
                    ?: error("Could not find status for job $jobId in Slurm cluster")
            logger.info { "Job $jobId status $status" }
            status
        }
    }

    override suspend fun jobLogs(jobId: String): String {
        return sshClient.runInSession {
            val (_, response) = executeCommand("cat $logsPath/${jobId.takeLast(JOB_ID_DIGITS)}/${jobId}_OUT")
            return@runInSession response
        }
    }

    private suspend fun await(
        job: Job,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ) {
        return sshClient.runInSession {
            waitUntil(
                checkInterval = ofSeconds(checkJobInterval),
                timeout = ofSeconds(maxSecondsDuration),
            ) {
                val status = jobStatus(job.id)
                val completed = status == "COMPLETED"
                if (completed.orFalse()) logger.info { "Job ${job.id} status is $status. Waiting for completion" }
                return@waitUntil completed
            }
        }
    }

    private fun asJobReturn(
        exitCode: Int,
        response: String,
    ): Result<Job> {
        if (exitCode == 0) return success(toSlurmJob(response, logsPath))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return failure(JobSubmitFailException(response))
    }

    companion object {
        fun create(
            sshKey: String,
            sshMachine: String,
            logsPath: String,
            wrapperPath: String,
        ): SlurmClusterClient =
            SlurmClusterClient(
                logsPath = logsPath,
                wrapperPath = wrapperPath,
                sshServer = sshMachine,
                sshKey = sshKey,
            )

        private fun JobSpec.asParameter(
            wrapperPath: String,
            logsPath: String,
        ): List<String> =
            buildList {
                add("--cores=$cores")
                add("--time=$minutes")
                add("--mem=$ram")
                add("--partition=${queue.name}")
                add("$wrapperPath/slurm_wrapper.sh \"$logsPath\" \"$command\"")
            }

        private const val JOB_ID_DIGITS = 3
    }
}
