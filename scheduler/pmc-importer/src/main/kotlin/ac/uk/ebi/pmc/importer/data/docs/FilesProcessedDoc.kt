package ac.uk.ebi.pmc.importer.data.docs

import java.time.Instant

data class FilesProcessedDoc(
    val name: String,
    val uploaded: Instant = Instant.now()
)