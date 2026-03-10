package uk.ac.ebi.biostd.client.cluster.common

import uk.ac.ebi.biostd.client.cluster.model.Job

private val slurmResponseRegex = "Submitted batch job (\\d+)\\s*\$".toRegex()

@Suppress("MagicNumber")
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
