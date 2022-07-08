package ac.uk.ebi.cluster.client.model

data class JobSpec(
    val cores: Int,
    val ram: MemorySpec,
    val command: String,
    val runAfter: List<Job> = emptyList(),
) {

    fun asParameter(): List<String> = buildList {
        add("-n")
        add(cores.toString())

        add("-M")
        add(ram.toString())

        add("-R")
        add("rusage[mem=$ram]")

        if (runAfter.isNotEmpty()) {
            add("-w")
            add("'${runAfter.joinToString(separator = " && ") { job -> "done(${job.id})" }}'")
        }

        add(command)
    }
}
