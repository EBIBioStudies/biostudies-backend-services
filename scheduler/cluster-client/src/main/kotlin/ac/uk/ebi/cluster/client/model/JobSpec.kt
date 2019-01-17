package ac.uk.ebi.cluster.client.model

import ebi.ac.uk.util.collections.ifNotEmpty

data class JobSpec(
    val cores: Int,
    val ram: MemorySpec,
    val command: String,
    val runAfter: List<Job> = emptyList()
) {

    fun asParameter() = mutableListOf<String>()
        .apply {
            add("-n")
            add(cores.toString())

            add("-M")
            add(ram.toString())

            add("-R")
            add("rusage[mem=$ram]")

            runAfter.ifNotEmpty {
                add("-w")
                add("'${it.joinToString(separator = " && ") { job -> "done(${job.id})" }}'")
            }

            add(command)
        }
}
