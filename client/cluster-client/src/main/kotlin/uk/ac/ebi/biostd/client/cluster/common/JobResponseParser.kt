package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val submitResponseRegex = ".*<(.*)>.*<(.*)>.*\\s".toRegex()

class JobResponseParser {

    fun toJob(submission: String): Job {
        val job = submitResponseRegex.matchEntire(submission)
            ?.destructured
            ?.let { (jobId, queue) -> Job(jobId, queue) }

        return job ?: throw IllegalAccessError("could not parse response")
    }
}
