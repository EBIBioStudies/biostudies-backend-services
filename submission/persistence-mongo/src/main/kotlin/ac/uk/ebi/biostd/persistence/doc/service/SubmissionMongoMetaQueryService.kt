package ac.uk.ebi.biostd.persistence.doc.service

import ac.uk.ebi.biostd.persistence.common.model.BasicCollection
import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.service.SubmissionMetaQueryService
import ac.uk.ebi.biostd.persistence.doc.db.data.SubmissionDocDataRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.asBasicSubmission
import ac.uk.ebi.biostd.persistence.exception.CollectionNotFoundException
import ac.uk.ebi.biostd.persistence.exception.CollectionWithoutPatternException
import ebi.ac.uk.model.constants.SubFields.ACC_NO_TEMPLATE
import java.time.ZoneOffset

class SubmissionMongoMetaQueryService(
    private val submissionDocDataRepository: SubmissionDocDataRepository
) : SubmissionMetaQueryService {

    override fun getBasicCollection(accNo: String): BasicCollection {
        val projectDb: DocSubmission? = submissionDocDataRepository.findByAccNo(accNo)
        require(projectDb != null) { throw CollectionNotFoundException(accNo) }

        val projectPattern = projectDb.attributes.firstOrNull { it.name == ACC_NO_TEMPLATE.value }?.value
            ?: throw CollectionWithoutPatternException(accNo)

        return BasicCollection(projectDb.accNo, projectPattern, projectDb.releaseTime?.atOffset(ZoneOffset.UTC))
    }

    override fun findLatestBasicByAccNo(accNo: String): BasicSubmission? =
        submissionDocDataRepository.findFirstByAccNoOrderByVersionDesc(accNo)?.asBasicSubmission()

    override fun getAccessTags(accNo: String): List<String> =
        submissionDocDataRepository.getCollections(accNo).map { it.accNo }

    override fun existByAccNo(accNo: String): Boolean = submissionDocDataRepository.existsByAccNo(accNo)
}
