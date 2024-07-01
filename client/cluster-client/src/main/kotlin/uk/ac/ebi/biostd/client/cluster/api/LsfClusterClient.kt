package uk.ac.ebi.biostd.client.cluster.api

import ebi.ac.uk.coroutines.waitUntil
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.common.JobSubmitFailException
import uk.ac.ebi.biostd.client.cluster.common.toLsfJob
import uk.ac.ebi.biostd.client.cluster.exception.FailedJobException
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.time.Duration.ofSeconds
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private val logger = KotlinLogging.logger {}

class LsfClusterClient(
    private val logsPath: String,
    private val sshMachine: String,
    private val sshKey: String,
) : ClusterClient {
    private val sshClient by lazy { SshClient(sshMachine = sshMachine, sshKey = sshKey) }

    override suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job> {
        val parameters = mutableListOf("bsub -o $logsPath/%J_OUT -e $logsPath/%J_IN")
        parameters.addAll(jobSpec.asParameter())
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

    override suspend fun jobStatus(jobId: String): String {
        return sshClient.runInSession {
            val status = executeCommand("bjobs -o STAT -noheader $jobId").second
            logger.info { "Job $jobId status $status" }
            status
        }
    }

    override suspend fun jobLogs(jobId: String): String {
        return sshClient.runInSession {
            val (_, response) = executeCommand("cat $logsPath/${jobId}_OUT")
            return@runInSession response
        }
    }

    private suspend fun await(
        job: Job,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ) {
        sshClient.runInSession {
            waitUntil(
                checkInterval = ofSeconds(checkJobInterval),
                timeout = ofSeconds(maxSecondsDuration),
            ) {
                val status = jobStatus(job.id)
                when (status) {
                    DONE_STATUS -> true
                    EXIT_STATUS -> throw FailedJobException(job)
                    else -> false
                }
            }
        }
    }

    private fun asJobReturn(
        exitCode: Int,
        response: String,
    ): Result<Job> {
        if (exitCode == 0) return success(toLsfJob(response))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return failure(JobSubmitFailException(response))
    }

    companion object {
        const val DONE_STATUS = "DONE"
        const val EXIT_STATUS = "EXIT"
        const val PEND_STATUS = "PEND"

        fun create(
            sshKey: String,
            sshMachine: String,
            logsPath: String,
        ): LsfClusterClient {
            return LsfClusterClient(logsPath = logsPath, sshKey = sshKey, sshMachine = sshMachine)
        }
    }

    private fun JobSpec.asParameter(): List<String> =
        buildList {
            add("-n")
            add(cores.toString())

            add("-M")
            add(ram.toString())

            add("-R")
            add("rusage[mem=$ram]")

            add("-q")
            add(queue.name)

            add(command)
        }
}
