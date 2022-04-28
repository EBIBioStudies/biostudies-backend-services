package ac.uk.ebi.biostd.persistence.filesystem.request

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY

data class FilePersistenceRequest(
    val submission: ExtSubmission,
    val mode: FileMode = COPY
)
