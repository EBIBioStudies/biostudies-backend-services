package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val lsfResponseRegex = "<([^>]+)>".toRegex()
private val slurmResponseRegex = "Submitted batch job (\\d+)\\s*\$".toRegex()

fun toLsfJob(response: String): Job {
    val matches = lsfResponseRegex.findAll(response).toList()
    if (matches.size == 2) return Job(matches[0].groupValues[1], matches[1].groupValues[1])
    error("could not parse response, '$response'")
}

fun toSlurmJob(response: String): Job {
    val match = slurmResponseRegex.matchEntire(response)
    val job = match?.destructured?.let { (jobId) -> Job(jobId, "not-specified") }
    return job ?: error("could not parse response, '$response'")
}
