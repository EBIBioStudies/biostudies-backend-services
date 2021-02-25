package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission

interface CollectionDataService {
    fun findCollectionsByAccessTags(tags: List<String>): List<BasicSubmission>
}
