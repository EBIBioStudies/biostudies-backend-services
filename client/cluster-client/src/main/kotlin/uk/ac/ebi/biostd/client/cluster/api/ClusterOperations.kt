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
import java.io.File
import java.time.Duration.ofSeconds

private const val DONE_STATUS = "DONE"

private val logger = KotlinLogging.logger {}

class ClusterOperations(
    private val logsPath: String,
    private val responseParser: JobResponseParser,
    private val sessionFunction: () -> Session,
) {
    suspend fun triggerJobAsync(jobSpec: JobSpec): Try<Job> {
        logger.info { "Triggering Job $jobSpec" }
        val parameters = mutableListOf("bsub -o $logsPath -e $logsPath")
        parameters.addAll(jobSpec.asParameter())
        val command = parameters.joinToString(separator = " ")
        logger.info { "Executing command $parameters" }

        return runInSession {
            val (exitStatus, response) = executeCommand(command)
            return@runInSession asJobReturn(exitStatus, response, logsPath)
        }
    }

    fun jobLogs(jobId: String): File {
        return File("$logsPath/${jobId}_OUT")
    }

    suspend fun jobStatus(jobId: String): String {
        logger.info { "Checking Job id ='$jobId' status" }
        return runInSession {
            val status = executeCommand("bjobs -o STAT -noheader $jobId").second.trimIndent()
            logger.info { "Job $jobId. Current status $status" }
            status
        }
    }

    suspend fun triggerJobSync(
        jobSpec: JobSpec,
        checkJobInterval: Long = 30,
        maxSecondsDuration: Long = 60,
    ): Job {
        suspend fun await(job: Job) = runInSession {
            waitUntil(
                interval = ofSeconds(checkJobInterval),
                duration = ofSeconds(maxSecondsDuration)
            ) { jobStatus(job.id) == DONE_STATUS }
            job
        }

        return triggerJobAsync(jobSpec).fold({ throw it }, { await(it) })
    }

    private fun asJobReturn(exitCode: Int, response: String, logsPath: String): Try<Job> {
        if (exitCode == 0) return Try.just(responseParser.toJob(response, logsPath))

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
