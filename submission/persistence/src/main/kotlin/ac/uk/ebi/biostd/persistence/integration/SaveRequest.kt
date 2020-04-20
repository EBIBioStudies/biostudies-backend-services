package ac.uk.ebi.biostd.persistence.integration

import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.User

data class SaveRequest(
    val submission: ExtSubmission,
    val submitter: User,
    val fileMode: FileMode
)

enum class FileMode { MOVE, COPY }
