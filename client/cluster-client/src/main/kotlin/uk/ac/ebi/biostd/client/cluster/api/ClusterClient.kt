package uk.ac.ebi.biostd.client.cluster.api

import arrow.core.Try
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import ebi.ac.uk.coroutines.waitUntil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import uk.ac.ebi.biostd.client.cluster.common.JobResponseParser
import uk.ac.ebi.biostd.client.cluster.common.JobSubmitFailException
import uk.ac.ebi.biostd.client.cluster.model.Job
import uk.ac.ebi.biostd.client.cluster.model.JobSpec
import java.time.Duration.ofSeconds

private const val CHECK_COMMAND = "bjobs -o STAT -noheader %s"
private const val DONE_STATUS = "DONE"
private const val SUBMIT_COMMAND = "bsub -o %s/%%J_OUT -e %s%%J_IN"

private val logger = KotlinLogging.logger {}

class ClusterClient(
    private val logsPath: String,
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session,
) : ClusterOperations {
    override suspend fun triggerJob(jobSpec: JobSpec): Try<Job> {
        val parameters = mutableListOf(String.format(SUBMIT_COMMAND, logsPath, logsPath))
        parameters.addAll(jobSpec.asParameter())
        val command = parameters.joinToString(separator = " ")

        return runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response, logsPath)
        }
    }

    override suspend fun awaitJob(
        jobSpec: JobSpec,
        checkJobInterval: Long,
        maxSecondsDuration: Long,
    ): Job {
        suspend fun await(job: Job) = runInSession {
            logger.info { "Started job ${job.id}" }
            waitUntil(
                interval = ofSeconds(checkJobInterval),
                duration = ofSeconds(maxSecondsDuration),
            ) {
                val status = executeCommand(String.format(CHECK_COMMAND, job.id)).second.trimIndent()
                logger.info { "Waiting for job ${job.id}. Current status $status" }

                status == DONE_STATUS
            }

            logger.info { "Finished job ${job.id}" }
            job
        }

        return triggerJob(jobSpec).fold({ throw it }, { await(it) })
    }

    private fun asJobReturn(exitCode: Int, response: String, logsPath: String): Try<Job> {
        if (exitCode == 0) return Try.just(responseParser.toJob(response, logsPath))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return Try.raise(JobSubmitFailException(response))
    }

    companion object {
        private val responseParser = JobResponseParser()

        fun create(sshKey: String, sshMachine: String, logsPath: String): ClusterClient {
            val sshClient = JSch()
            sshClient.addIdentity(sshKey)
            return ClusterClient(logsPath, responseParser) {
                val session = sshClient.getSession(sshMachine)
                session.setConfig("StrictHostKeyChecking", "no")
                return@ClusterClient session
            }
        }
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
}
