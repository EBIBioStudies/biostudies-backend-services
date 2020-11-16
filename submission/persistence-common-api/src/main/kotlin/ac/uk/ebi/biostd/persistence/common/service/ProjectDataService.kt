package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.SimpleSubmission

interface ProjectDataService {
    fun findProjectsByAccessTags(tags: List<String>): List<SimpleSubmission>
}
