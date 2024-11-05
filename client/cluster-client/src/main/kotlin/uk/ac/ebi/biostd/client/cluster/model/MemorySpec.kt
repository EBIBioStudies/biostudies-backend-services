package uk.ac.ebi.biostd.client.cluster.model

data class MemorySpec(val value: Int, val measure: CapacityMeasure) {
    override fun toString() = (value * measure.multiplier).toString()

    companion object {
        val ONE_GB = MemorySpec(1, CapacityMeasure.GB)
        val EIGHT_GB = MemorySpec(8, CapacityMeasure.GB)
        val SIXTEEN_GB = MemorySpec(16, CapacityMeasure.GB)
        val TWENTY_FOUR_GB = MemorySpec(24, CapacityMeasure.GB)

        fun fromMegaBytes(megaBytes: Int): MemorySpec {
            return MemorySpec(megaBytes, CapacityMeasure.MB)
        }
    }
}
