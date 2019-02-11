package ac.uk.ebi.pmc.persistence.docs

import java.time.Instant

data class FilesProcessedDoc(
    val name: String,
    val uploaded: Instant = Instant.now()
)
