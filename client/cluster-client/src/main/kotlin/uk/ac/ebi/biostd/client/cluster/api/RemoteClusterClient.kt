package uk.ac.ebi.biostd.client.cluster.api

import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import ebi.ac.uk.coroutines.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.common.JobResponseParser
import uk.ac.ebi.biostd.client.cluster.common.JobSubmitFailException
import uk.ac.ebi.biostd.client.cluster.exception.FailedJobException
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.time.Duration.ofSeconds
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

private val logger = KotlinLogging.logger {}

class RemoteClusterClient(
    private val logsPath: String,
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session,
) : ClusterClient {
    override suspend fun triggerJobAsync(jobSpec: JobSpec): Result<Job> {
        val parameters = mutableListOf("bsub -o $logsPath/%J_OUT -e $logsPath/%J_IN")
        parameters.addAll(jobSpec.asParameter())
        val command = parameters.joinToString(separator = " ")
        logger.info { "Executing command '$command'" }

        return runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response, logsPath)
        }
    }

    override suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ): Job {
        return triggerJobAsync(jobSpec).fold({ await(it, checkJobInterval, maxSecondsDuration) }, { throw it })
    }

    override suspend fun jobStatus(jobId: String): String {
        return runInSession {
            val status = executeCommand("bjobs -o STAT -noheader $jobId").second.trimIndent()
            logger.info { "Job $jobId status $status" }
            status
        }
    }

    override suspend fun jobLogs(jobId: String): String {
        return runInSession {
            val (_, response) = executeCommand("cat $logsPath/${jobId}_OUT")
            return@runInSession response
        }
    }

    private suspend fun await(
        job: Job,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ): Job {
        return runInSession {
            var status: String = PEND_STATUS
            waitUntil(
                checkInterval = ofSeconds(checkJobInterval),
                timeout = ofSeconds(maxSecondsDuration),
            ) {
                status = jobStatus(job.id)
                val executionFinished = status == DONE_STATUS || status == EXIT_STATUS
                when (status) {
                    EXIT_STATUS -> logger.error { "Job ${job.id} status is $EXIT_STATUS. Execution failed" }
                    DONE_STATUS -> logger.info { "Job ${job.id} status is $DONE_STATUS. Execution completed" }
                    else -> logger.info { "Job ${job.id} status is $status. Waiting for completion" }
                }

                return@waitUntil executionFinished
            }

            if (status == DONE_STATUS) job else throw FailedJobException(job)
        }
    }

    private fun asJobReturn(
        exitCode: Int,
        response: String,
        logsPath: String,
    ): Result<Job> {
        if (exitCode == 0) return success(responseParser.toJob(response, logsPath))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return failure(JobSubmitFailException(response))
    }

    private suspend fun <T> runInSession(exec: suspend CommandRunner.() -> T): T {
        return withContext(Dispatchers.IO) {
            val session = sessionFunction()

            try {
                session.connect()
                exec(CommandRunner(session))
            } finally {
                session.disconnect()
            }
        }
    }

    companion object {
        internal const val DONE_STATUS = "DONE"
        internal const val EXIT_STATUS = "EXIT"
        internal const val PEND_STATUS = "PEND"

        private val responseParser = JobResponseParser()

        fun create(
            sshKey: String,
            sshMachine: String,
            logsPath: String,
        ): RemoteClusterClient {
            val sshClient = JSch()
            sshClient.addIdentity(sshKey)
            return RemoteClusterClient(logsPath, responseParser) {
                val session = sshClient.getSession(sshMachine)
                session.setConfig("StrictHostKeyChecking", "no")
                return@RemoteClusterClient session
            }
        }
    }
}
