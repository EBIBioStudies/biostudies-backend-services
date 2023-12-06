package uk.ac.ebi.biostd.client.cluster.model

sealed class QueueSpec(val name: String) {
    companion object {
        const val STANDARD = "standard"
        const val DATA_MOVER = "datamover"

        fun fromName(queue: String): QueueSpec {
            return when (queue) {
                STANDARD -> StandardQueue
                DATA_MOVER -> DataMoverQueue
                else -> throw IllegalArgumentException("invalid queue $queue")
            }
        }
    }
}

object StandardQueue : QueueSpec(STANDARD)
object DataMoverQueue : QueueSpec(DATA_MOVER)
