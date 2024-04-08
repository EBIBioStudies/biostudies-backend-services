package uk.ac.ebi.biostd.client.cluster.model

@Suppress("MagicNumber")
enum class CapacityMeasure(val multiplier: Int) {
    MB(1),
    GB(1024),
}
