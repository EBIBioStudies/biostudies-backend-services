package ac.uk.ebi.biostd.persistence.filesystem.request

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY

typealias Md5 = String

data class FilePersistenceRequest(
    val submission: ExtSubmission,
    val mode: FileMode = COPY,
    val previousFiles: Map<Md5, ExtFile> = emptyMap()
)