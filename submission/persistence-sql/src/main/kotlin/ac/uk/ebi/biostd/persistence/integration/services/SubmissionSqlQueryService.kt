package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.common.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.common.exception.CollectionWithoutPatternException
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.CollectionSqlDataService.Companion.asBasicSubmission
import ebi.ac.uk.model.constants.SubFields
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import ebi.ac.uk.model.constants.SubFields.COLLECTION_VALIDATOR

@Suppress("TooManyFunctions")
internal class SubmissionSqlQueryService(
    private val subRepository: SubmissionDataRepository,
    private val accessTagDataRepo: AccessTagDataRepo
) : SubmissionMetaQueryService {
    override fun getBasicCollection(accNo: String): BasicCollection {
        val collection = subRepository.findBasicWithAttributes(accNo)
        require(collection != null) { throw CollectionNotFoundException(accNo) }

        val validator = collection.attrValue(COLLECTION_VALIDATOR)
        val collectionPattern = collection.attrValue(ACC_NO_TEMPLATE) ?: throw CollectionWithoutPatternException(accNo)

        return BasicCollection(collection.accNo, collectionPattern, validator, collection.releaseTime)
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        subRepository.getLastVersion(accNo)?.asBasicSubmission()

    override fun getAccessTags(accNo: String) = accessTagDataRepo.findBySubmissionsAccNo(accNo).map { it.name }

    override fun existByAccNo(accNo: String): Boolean = subRepository.existsByAccNo(accNo)

    private fun DbSubmission.attrValue(name: SubFields) = attributes.firstOrNull { it.name == name.value }?.value
}
