package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicProject
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.exception.ProjectNotFoundException
import ac.uk.ebi.biostd.persistence.exception.ProjectWithoutPatternException
import ebi.ac.uk.model.SubmissionMethod
import ebi.ac.uk.model.constants.ProcessingStatus
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

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? {
        return submissionDocDataRepository.findFirstByAccNoOrderByVersionDesc(accNo)?.asBasicSubmission()
    }

    override fun getAccessTags(accNo: String): List<String> {
        return emptyList()
    }

    override fun existByAccNo(accNo: String): Boolean =
        submissionDocDataRepository.existsByAccNo(accNo)

    companion object {

        private fun DocProcessingStatus.toProcessingStatus(): ProcessingStatus {
            return when (this) {
                DocProcessingStatus.PROCESSED -> ProcessingStatus.PROCESSED
                DocProcessingStatus.PROCESSING -> ProcessingStatus.PROCESSING
                DocProcessingStatus.REQUESTED -> ProcessingStatus.REQUESTED
            }
        }

        private fun DocSubmissionMethod.toSubmissionMethod(): SubmissionMethod {
            return when (this) {
                DocSubmissionMethod.FILE -> SubmissionMethod.FILE
                DocSubmissionMethod.PAGE_TAB -> SubmissionMethod.PAGE_TAB
                DocSubmissionMethod.UNKNOWN -> SubmissionMethod.UNKNOWN
            }
        }

        fun DocSubmission.asBasicSubmission(): BasicSubmission {
            return BasicSubmission(
                accNo = accNo,
                version = version,
                secretKey = secretKey,
                title = title,
                relPath = relPath,
                released = released,
                creationTime = creationTime.atOffset(ZoneOffset.UTC),
                modificationTime = modificationTime.atOffset(ZoneOffset.UTC),
                releaseTime = releaseTime?.atOffset(ZoneOffset.UTC),
                status = status.toProcessingStatus(),
                method = method.toSubmissionMethod(),
                owner = owner)
        }
    }
}
