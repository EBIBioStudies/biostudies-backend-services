package uk.ac.ebi.biostd.client.cluster.model

import uk.ac.ebi.biostd.client.cluster.model.CoresSpec.ONE_CORE
import uk.ac.ebi.biostd.client.cluster.model.MemorySpec.Companion.ONE_GB

data class JobSpec(
    val cores: Int = ONE_CORE,
    val ram: MemorySpec = ONE_GB,
    val queue: QueueSpec = StandardQueue,
    val minutes: Int = 60,
    val command: String,
)
