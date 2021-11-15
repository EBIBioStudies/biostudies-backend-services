package ac.uk.ebi.biostd.persistence.filesystem.pagetab

import ebi.ac.uk.extended.model.ExtSubmission

interface PageTabService {
    fun generatePageTab(sub: ExtSubmission): ExtSubmission
}
