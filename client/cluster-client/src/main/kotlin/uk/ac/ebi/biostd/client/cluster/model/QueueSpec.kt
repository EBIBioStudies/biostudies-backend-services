package uk.ac.ebi.biostd.client.cluster.model

sealed class QueueSpec(val name: String) {
    companion object {
        const val STANDARD = "standard"
        const val DATA_MOVER = "datamover"
    }
}

object StandardQueue : QueueSpec(STANDARD)
object DataMoverQueue : QueueSpec(DATA_MOVER)
