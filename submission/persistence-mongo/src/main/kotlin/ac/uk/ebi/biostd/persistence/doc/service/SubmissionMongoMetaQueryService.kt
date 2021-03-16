package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.exception.CollectionWithoutPatternException
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR
import java.time.ZoneOffset.UTC

class SubmissionMongoMetaQueryService(
    private val submissionDocDataRepository: SubmissionDocDataRepository
) : SubmissionMetaQueryService {
    override fun getBasicCollection(accNo: String): BasicCollection {
        val collection: DocSubmission? = submissionDocDataRepository.findByAccNo(accNo)
        require(collection != null) { throw CollectionNotFoundException(accNo) }

        val validator = collection.attrValue(COLLECTION_VALIDATOR)
        val collectionPattern = collection.attrValue(ACC_NO_TEMPLATE) ?: throw CollectionWithoutPatternException(accNo)

        return BasicCollection(collection.accNo, collectionPattern, validator, collection.releaseTime?.atOffset(UTC))
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        submissionDocDataRepository.findFirstByAccNoOrderByVersionDesc(accNo)?.asBasicSubmission()

    override fun getAccessTags(accNo: String): List<String> =
        submissionDocDataRepository.getCollections(accNo).map { it.accNo }

    override fun existByAccNo(accNo: String): Boolean = submissionDocDataRepository.existsByAccNo(accNo)

    private fun DocSubmission.attrValue(name: SubFields) = attributes.firstOrNull { it.name == name.value }?.value
}
