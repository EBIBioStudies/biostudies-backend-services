package ac.uk.ebi.cluster.client.lsf

import ac.uk.ebi.cluster.client.common.JobResponseParser
import ac.uk.ebi.cluster.client.common.JobSubmitFailException
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import arrow.core.Try
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import ebi.ac.uk.coroutines.waitUntil
import mu.KotlinLogging
import java.time.Duration

private const val CHECK_COMMAND = "bjobs -o STAT -noheader %s"
private const val DONE_STATUS = "DONE"
private const val SUBMIT_COMMAND = "bsub -o %s/%%J_OUT -e %s%%J_IN"

private val logger = KotlinLogging.logger {}

// TODO move this to :client:cluster-client
class ClusterOperations(
    private val logsPath: String,
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session,
) {
    suspend fun triggerJob(jobSpec: JobSpec): Try<Job> {
        val parameters = mutableListOf(String.format(SUBMIT_COMMAND, logsPath, logsPath))
        parameters.addAll(jobSpec.asParameter())
        val command = parameters.joinToString(separator = " ")

        return runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response)
        }
    }

    suspend fun awaitJob(
        jobSpec: JobSpec,
        maxSecondsDuration: Long = 60,
    ): Job {
        suspend fun await(job: Job) = runInSession {
            waitUntil(Duration.ofSeconds(maxSecondsDuration)) {
                executeCommand(String.format(CHECK_COMMAND, job.id)).second.trimIndent() == DONE_STATUS
            }

            job
        }

        return triggerJob(jobSpec).fold({ throw it }, { await(it) })
    }

    private fun asJobReturn(exitCode: Int, response: String): Try<Job> {
        if (exitCode == 0) return Try.just(responseParser.toJob(response))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return Try.raise(JobSubmitFailException(response))
    }

    companion object {
        private val responseParser = JobResponseParser()

        fun create(sshKey: String, sshMachine: String, logsPath: String): ClusterOperations {
            val sshClient = JSch()
            sshClient.addIdentity(sshKey)
            return ClusterOperations(logsPath, responseParser) {
                val session = sshClient.getSession(sshMachine)
                session.setConfig("StrictHostKeyChecking", "no")
                return@ClusterOperations session
            }
        }
    }

    private suspend fun <T> runInSession(exec: suspend CommandRunner.() -> T): T {
        val session = sessionFunction()

        try {
            session.connect()
            return exec(CommandRunner(session))
        } finally {
            session.disconnect()
        }
    }
}
