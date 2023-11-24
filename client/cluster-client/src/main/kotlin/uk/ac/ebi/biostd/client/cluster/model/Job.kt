package uk.ac.ebi.biostd.client.cluster.model

data class Job(
    val id: String,
    val queue: String,
    val logsPath: String,
)
