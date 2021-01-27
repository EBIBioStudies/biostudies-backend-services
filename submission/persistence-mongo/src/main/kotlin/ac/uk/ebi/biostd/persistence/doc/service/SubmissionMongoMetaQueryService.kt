package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.exception.ProjectWithoutPatternException
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import java.time.ZoneOffset

class SubmissionMongoMetaQueryService(
    private val submissionDocDataRepository: SubmissionDocDataRepository
) : SubmissionMetaQueryService {

    override fun getBasicProject(accNo: String): BasicProject {
        val projectDb: DocSubmission? = submissionDocDataRepository.findByAccNo(accNo)
        require(projectDb != null) { throw ProjectNotFoundException(accNo) }

        val projectPattern = projectDb.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.value }?.value
            ?: throw ProjectWithoutPatternException(accNo)

        return BasicProject(projectDb.accNo, projectPattern, projectDb.releaseTime?.atOffset(ZoneOffset.UTC))
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        submissionDocDataRepository.findFirstByAccNoOrderByVersionDesc(accNo)?.asBasicSubmission()

    override fun getAccessTags(accNo: String): List<String> {
        return emptyList()
    }

    override fun existByAccNo(accNo: String): Boolean = submissionDocDataRepository.existsByAccNo(accNo)
}
