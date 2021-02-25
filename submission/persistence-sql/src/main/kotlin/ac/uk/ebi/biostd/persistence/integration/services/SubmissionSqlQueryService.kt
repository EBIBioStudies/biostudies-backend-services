package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.exception.CollectionWithoutPatternException
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.CollectionSqlDataService.Companion.asBasicSubmission
import ebi.ac.uk.model.constants.SubFields

@Suppress("TooManyFunctions")
internal class SubmissionSqlQueryService(
    private val subRepository: SubmissionDataRepository,
    private val accessTagDataRepo: AccessTagDataRepo
) : SubmissionMetaQueryService {
    override fun getBasicCollection(accNo: String): BasicCollection {
        val collectionDb = subRepository.findBasicWithAttributes(accNo)
        require(collectionDb != null) { throw CollectionNotFoundException(accNo) }

        val collectionPattern =
            collectionDb.attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.value }
                ?.value ?: throw CollectionWithoutPatternException(accNo)

        return BasicCollection(collectionDb.accNo, collectionPattern, collectionDb.releaseTime)
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        subRepository.getLastVersion(accNo)?.asBasicSubmission()

    override fun getAccessTags(accNo: String) = accessTagDataRepo.findBySubmissionsAccNo(accNo).map { it.name }

    override fun existByAccNo(accNo: String): Boolean = subRepository.existsByAccNo(accNo)
}
