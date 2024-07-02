package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val lsfResponseRegex = ".*<(.*)>.*<(.*)>.*\\s".toRegex()

fun toLsfJob(response: String): Job {
    val match = lsfResponseRegex.matchEntire(response)
    val job = match?.destructured?.let { (jobId, queue) -> Job(jobId, queue) }
    return job ?: throw IllegalAccessError("could not parse response, '$response'")
}
