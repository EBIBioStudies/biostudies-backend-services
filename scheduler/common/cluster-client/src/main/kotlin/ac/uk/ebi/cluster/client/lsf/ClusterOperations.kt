package ac.uk.ebi.cluster.client.lsf

import ac.uk.ebi.cluster.client.common.JobResponseParser
import ac.uk.ebi.cluster.client.common.JobSubmitFailException
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import arrow.core.Try
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import mu.KotlinLogging

private const val AS_SYSTEM_USER = "sudo -u $SYSTEM_USER $LSF_ENVDIR $LSF_SERVERDIR"
private const val REDIRECT_LOGS = "-o $LOGS_PATH%J_OUT -e $LOGS_PATH/%J_IN"
private const val SUBMIT_COMMAND = "$AS_SYSTEM_USER bsub $REDIRECT_LOGS"
private val logger = KotlinLogging.logger {}

class ClusterOperations(
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session,
) {

    fun triggerJob(jobSpec: JobSpec): Try<Job> {
        val parameters = mutableListOf(SUBMIT_COMMAND)
        parameters.addAll(jobSpec.asParameter())
        val command = parameters.joinToString(separator = " ")

        return runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response)
        }
    }

    private fun asJobReturn(exitCode: Int, response: String): Try<Job> {
        if (exitCode == 0) return Try.just(responseParser.toJob(response))

        logger.error(response) { "Error submission job, exitCode='$exitCode', response='$response'" }
        return Try.raise(JobSubmitFailException(response))
    }

    companion object {
        private val responseParser = JobResponseParser()

        fun create(sshKey: String, sshMachine: String): ClusterOperations {
            val sshClient = JSch()
            sshClient.addIdentity(sshKey)
            return ClusterOperations(responseParser) {
                val session = sshClient.getSession(sshMachine)
                session.setConfig("StrictHostKeyChecking", "no")
                return@ClusterOperations session
            }
        }
    }

    private fun <T> runInSession(exec: CommandRunner.() -> T): T {
        val session = sessionFunction()

        try {
            session.connect()
            return exec(CommandRunner(session))
        } finally {
            session.disconnect()
        }
    }
}
