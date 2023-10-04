package ac.uk.ebi.cluster.client.model

import ac.uk.ebi.cluster.client.model.CoresSpec.ONE_CORE
import ac.uk.ebi.cluster.client.model.MemorySpec.Companion.ONE_GB

data class JobSpec(
    val cores: Int = ONE_CORE,
    val ram: MemorySpec = ONE_GB,
    val queue: QueueSpec = StandardQueue,
    val command: String,
) {

    fun asParameter(): List<String> = buildList {
        add("-n")
        add(cores.toString())

        add("-M")
        add(ram.toString())

        add("-R")
        add("rusage[mem=$ram]")

        add("-q")
        add(queue.name)

        add(command)
    }
}
