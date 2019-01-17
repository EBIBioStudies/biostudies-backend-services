package ac.uk.ebi.cluster.client.model

data class MemorySpec(val value: Int, val measure: CapacityMeasure) {

    override fun toString(): String {
        return (value * measure.multiplier).toString()
    }

    companion object {
        val ONE_GB = MemorySpec(1, CapacityMeasure.GB)
        val EIGHT_GB = MemorySpec(8, CapacityMeasure.GB)
        val TEN_GB = MemorySpec(10, CapacityMeasure.GB)
        val SIXTEEN_GB = MemorySpec(16, CapacityMeasure.GB)
    }
}
