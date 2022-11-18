package ac.uk.ebi.biostd.persistence.doc.mapping.to

data class SubInfo(
    val accNo: String,
    val version: Int,
    val released: Boolean,
    val relPath: String,
)
