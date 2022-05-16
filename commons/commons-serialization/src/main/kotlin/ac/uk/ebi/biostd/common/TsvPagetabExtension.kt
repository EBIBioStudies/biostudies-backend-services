package ac.uk.ebi.biostd.common

class TsvPagetabExtension(
    private val featureEnabled: Boolean
) {
    fun tsvExtension(): String = if (featureEnabled) "tsv" else "pagetab.tsv"
}
