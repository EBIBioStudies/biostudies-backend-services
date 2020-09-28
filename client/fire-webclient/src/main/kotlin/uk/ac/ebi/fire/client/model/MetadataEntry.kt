package uk.ac.ebi.fire.client.model

data class MetadataEntry(
    val key: String,
    val value: String
) {
    override fun toString(): String = "\"${key}\": \"${value}\""
}
