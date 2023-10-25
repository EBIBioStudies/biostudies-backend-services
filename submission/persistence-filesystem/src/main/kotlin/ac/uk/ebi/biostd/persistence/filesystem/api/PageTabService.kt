package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtSubmission

internal interface PageTabService {
    suspend fun generatePageTab(sub: ExtSubmission): ExtSubmission
}
