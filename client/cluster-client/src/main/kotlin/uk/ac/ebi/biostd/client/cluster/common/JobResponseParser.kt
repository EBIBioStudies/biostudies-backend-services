package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val lsfResponseRegex = "<([^>]+)>".toRegex()
private val slurmResponseRegex = "Submitted batch job (\\d+)\\s*\$".toRegex()

fun toLsfJob(response: String): Job {
    val matches = lsfResponseRegex.findAll(response).toList()
    if (matches.size == 2) return Job(matches[0].groupValues[1], matches[1].groupValues[1], "not-specified")
    error("could not parse response, '$response'")
}

fun toSlurmJob(
    response: String,
    logsPath: String,
): Job {
    val match = slurmResponseRegex.matchEntire(response)
    val job =
        match?.destructured?.let { (jobId) ->
            Job(id = jobId, queue = "not-specified", logsPath = "$logsPath/${jobId.takeLast(3)}/${jobId}_OUT")
        }
    return job ?: error("could not parse response, '$response'")
}
