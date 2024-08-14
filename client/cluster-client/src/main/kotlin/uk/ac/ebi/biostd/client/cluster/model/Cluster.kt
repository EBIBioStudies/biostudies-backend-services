package uk.ac.ebi.biostd.client.cluster.model

enum class Cluster {
    LSF,
    SLURM,
    ;

    companion object {
        fun fromName(name: String): Cluster {
            return when (name.uppercase()) {
                "LSF" -> LSF
                "SLURM" -> SLURM
                else -> throw IllegalArgumentException("$name is not a valid cluster name")
            }
        }
    }
}
