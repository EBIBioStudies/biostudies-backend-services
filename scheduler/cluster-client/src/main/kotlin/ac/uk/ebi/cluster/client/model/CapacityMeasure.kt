package ac.uk.ebi.cluster.client.model

@Suppress("MagicNumber")
enum class CapacityMeasure(val multiplier: Int) {
    MB(1), GB(1024)
}
