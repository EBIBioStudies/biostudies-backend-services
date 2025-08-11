package ac.uk.ebi.biostd.persistence.filesystem.api

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission

internal interface PageTabService {
    suspend fun generatePageTab(sub: ExtSubmission): ExtSubmission

    suspend fun generatePageTab(
        sub: ExtSubmission,
        anonymize: Boolean,
    ): List<ExtFile>
}
