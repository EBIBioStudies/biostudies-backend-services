package ac.uk.ebi.cluster.client.model

import ac.uk.ebi.cluster.client.lsf.LOGS_PATH

data class Job(val id: String, val queue: String)

val Job.logsPath: String
    get() = "$LOGS_PATH${id}_OUT"
