package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission

interface ProjectDataService {
    fun findProjectsByAccessTags(tags: List<String>): List<BasicSubmission>
}
