package ac.uk.ebi.biostd.persistence.common.service

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import kotlinx.coroutines.flow.Flow

interface CollectionDataService {
    fun findCollectionsByAccessTags(tags: List<String>): Flow<BasicSubmission>
}
