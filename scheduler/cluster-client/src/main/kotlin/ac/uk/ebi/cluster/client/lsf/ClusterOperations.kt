package ac.uk.ebi.cluster.client.lsf

import ac.uk.ebi.cluster.client.common.JobResponseParser
import ac.uk.ebi.cluster.client.common.JobSubmitFailException
import ac.uk.ebi.cluster.client.model.Job
import ac.uk.ebi.cluster.client.model.JobSpec
import arrow.core.Try
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session

private const val AS_SYSTEM_USER = "sudo -u $SYSTEM_USER $LSF_ENVDIR $LSF_SERVERDIR"
private const val REDIRECT_LOGS = "-o $LOGS_PATH%J_OUT -e $LOGS_PATH/%J_IN"
private const val SUBMIT_COMMAND = "$AS_SYSTEM_USER bsub $REDIRECT_LOGS"

class ClusterOperations(
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session
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

    private fun asJobReturn(exitCode: Int, response: String) =
        if (exitCode == 0)
            Try.just(responseParser.toJob(response))
        else
            Try.raise(JobSubmitFailException(response))

    companion object {

        private val sshClient = JSch()
        private val responseParser = JobResponseParser()

        fun create(user: String, password: String?, sshMachine: String): ClusterOperations {
            return ClusterOperations(responseParser) { createSession(user, password, sshMachine) }
        }

        private fun createSession(user: String, password: String?, sshMachine: String): Session {
            val session = sshClient.getSession(user, sshMachine)
            session.setConfig("StrictHostKeyChecking", "no")
            password?.let { session.setPassword(it) }
            return session
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
