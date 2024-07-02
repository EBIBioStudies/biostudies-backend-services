package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val lsfResponseRegex = ".*<(.*)>.*<(.*)>.*\\s".toRegex()
private val slurmResponseRegex = "Submitted batch job (\\d+)\\s*\$".toRegex()

fun toLsfJob(response: String): Job {
    val match = lsfResponseRegex.matchEntire(response)
    val job = match?.destructured?.let { (jobId, queue) -> Job(jobId, queue) }
    return job ?: throw IllegalAccessError("could not parse response, '$response'")
}

fun toSlurmJob(response: String): Job {
    val match = slurmResponseRegex.matchEntire(response)
    val job = match?.destructured?.let { (jobId) -> Job(jobId, "not-specified") }
    return job ?: throw IllegalAccessError("could not parse response, '$response'")
}
