package uk.ac.ebi.biostd.client.cluster.model

import uk.ac.ebi.biostd.client.cluster.lsf.LOGS_PATH

data class Job(
    val id: String,
    val queue: String,
) {
    val logsPath: String = "$LOGS_PATH${id}_OUT"
}
