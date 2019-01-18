package ac.uk.ebi.pmc.importer.data.docs

import java.time.Instant

data class ErrorDoc(
    val id: String,
    val body: String,
    val sourceFile: String,
    val error: String,
    val uploaded: Instant = Instant.now()
)
