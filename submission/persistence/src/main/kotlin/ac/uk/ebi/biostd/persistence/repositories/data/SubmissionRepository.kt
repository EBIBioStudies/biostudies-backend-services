package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilter
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilterSpecification
import ac.uk.ebi.biostd.persistence.mapping.extended.to.DbToExtRequest
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.pagination.OffsetPageRequest
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission
import ac.uk.ebi.biostd.persistence.projections.SimpleSubmission.Companion.asSimpleSubmission
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs
import ebi.ac.uk.extended.mapping.to.toSimpleSubmission
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.model.Submission
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}
private val defaultOrder = Order.asc("id")

@Suppress("TooManyFunctions")
open class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val sectionRepository: SectionDataRepository,
    private val statsRepository: SubmissionStatsDataRepository,
    private var submissionMapper: ToExtSubmissionMapper
) {
    @Transactional(readOnly = true)
    open fun getDbSubmission(accNo: String, version: Int): DbSubmission = lodSubmission(accNo, version)

    @Transactional(readOnly = true)
    open fun getSimpleByAccNo(accNo: String): Submission = getExtByAccNo(accNo).toSimpleSubmission()

    @Transactional(readOnly = true)
    open fun getExtByAccNo(accNo: String) = submissionMapper.toExtSubmission(dbToExtRequest(accNo))

    @Transactional(readOnly = true)
    open fun getExtByAccNoAndVersion(accNo: String, version: Int) =
        submissionMapper.toExtSubmission(dbToExtRequest(accNo, version))

    open fun expireSubmission(accNo: String) {
        val submission = submissionRepository.findByAccNoAndVersionGreaterThan(accNo)
        if (submission != null) {
            submission.version = -submission.version
            submissionRepository.save(submission)
        }
    }

    @Transactional(readOnly = true)
    open fun getExtendedSubmissions(offset: Long, limit: Int): Page<ExtSubmission> =
        submissionRepository
            .getIds(OffsetPageRequest(offset, limit, Sort.by(defaultOrder)))
            .map { getExtByAccNoAndVersion(it.accNo, it.version) }

    open fun getSubmissionsByUser(userId: Long, filter: SubmissionFilter): List<SimpleSubmission> {
        val filterSpecs = SubmissionFilterSpecification(userId, filter)
        val pageable = PageRequest.of(filter.pageNumber, filter.limit, Sort.by("releaseTime").descending())
        return submissionRepository
            .findAll(filterSpecs.specification, pageable, EntityGraphs.named(SimpleSubmission.SIMPLE_GRAPH))
            .content
            .map { it.asSimpleSubmission() }
    }

    private fun dbToExtRequest(accNo: String, version: Int? = null): DbToExtRequest =
        DbToExtRequest(lodSubmission(accNo, version), statsRepository.findByAccNo(accNo))

    /**
     * Load submission information strategy used is basically first load submission and then load each section and its
     * subsections recursively starting from the root section.
     */
    private fun lodSubmission(accNo: String, version: Int? = null): DbSubmission {
        logger.debug { "loading submission $accNo" }

        val submission = findSubmission(accNo, version)
        submission ?: throw SubmissionNotFoundException(accNo)
        loadSection(submission.rootSectionId)

        logger.debug { "Loaded submission $accNo" }
        return submission
    }

    private fun findSubmission(accNo: String, version: Int?): DbSubmission? {
        return when (version) {
            null -> submissionRepository.getByAccNoAndVersionGreaterThan(accNo, 0)
            else -> submissionRepository.getByAccNoAndVersion(accNo, version)
        }
    }

    private fun loadSection(sectionId: Long) {
        val section = sectionRepository.getById(sectionId)
        section.sections.forEach { loadSection(it.id) }
    }
}
