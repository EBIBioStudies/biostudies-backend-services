package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.CollectionWithoutPatternException
import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ebi.ac.uk.model.constants.ProcessingStatus.PROCESSED
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR
import java.time.ZoneOffset.UTC

class SubmissionMongoMetaQueryService(
    private val submissionRepository: SubmissionDocDataRepository
) : SubmissionMetaQueryService {
    override fun getBasicCollection(accNo: String): BasicCollection {
        val collection = submissionRepository.findByAccNo(accNo) ?: throw CollectionNotFoundException(accNo)
        val collectionPattern = collection.attrValue(ACC_NO_TEMPLATE) ?: throw CollectionWithoutPatternException(accNo)
        val validator = collection.attrValue(COLLECTION_VALIDATOR)

        return BasicCollection(collection.accNo, collectionPattern, validator, collection.releaseTime?.atOffset(UTC))
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        submissionRepository.findByAccNo(accNo)?.asBasicSubmission(PROCESSED)

    override fun getAccessTags(accNo: String): List<String> =
        submissionRepository.getCollections(accNo).map { it.accNo }

    override fun existByAccNo(accNo: String): Boolean = submissionRepository.existsByAccNo(accNo)

    private fun DocSubmission.attrValue(name: SubFields) = attributes.firstOrNull { it.name == name.value }?.value
}
