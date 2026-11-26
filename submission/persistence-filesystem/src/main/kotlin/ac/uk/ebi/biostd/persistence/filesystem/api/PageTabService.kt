package ac.uk.ebi.biostd.persistence.filesystem.api

import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtSubmission

internal interface PageTabService {
    suspend fun generatePageTab(sub: ExtSubmission): ExtSubmission

    suspend fun generatePageTab(
        sub: ExtSubmission,
        anonymize: Boolean,
    ): List<ExtFile>
}
