package ac.uk.ebi.biostd.persistence.integration

import ebi.ac.uk.extended.model.ExtSubmission

data class SaveRequest(
    val submission: ExtSubmission,
    val fileMode: FileMode
) {
    operator fun component3() = submission.accNo
}

enum class FileMode { MOVE, COPY }
