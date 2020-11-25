package ac.uk.ebi.biostd.persistence.integration.services

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
import ac.uk.ebi.biostd.persistence.common.model.SimpleSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.exception.ProjectWithoutPatternException
import ac.uk.ebi.biostd.persistence.repositories.AccessTagDataRepo
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.ProjectSqlDataService.Companion.asSimpleSubmission
import ebi.ac.uk.model.constants.SubFields

@Suppress("TooManyFunctions")
internal class SubmissionSqlQueryService(
    private val subRepository: SubmissionDataRepository,
    private val accessTagDataRepo: AccessTagDataRepo
) : SubmissionMetaQueryService {
    override fun getBasicProject(accNo: String): BasicProject {
        val projectDb = subRepository.findBasicWithAttributes(accNo)
        require(projectDb != null) { throw ProjectNotFoundException(accNo) }

        val projectPattern =
            projectDb.attributes.firstOrNull { it.name == SubFields.ACC_NO_TEMPLATE.value }
            ?.value ?: throw ProjectWithoutPatternException(accNo)

        return BasicProject(projectDb.accNo, projectPattern, projectDb.releaseTime)
    }

    override fun findLatestBasicByAccNo(accNo: String): SimpleSubmission? =
        subRepository.getLastVersion(accNo)?.asSimpleSubmission()

    override fun getAccessTags(accNo: String) = accessTagDataRepo.findBySubmissionsAccNo(accNo).map { it.name }

    override fun existByAccNo(accNo: String): Boolean = subRepository.existsByAccNo(accNo)
}
