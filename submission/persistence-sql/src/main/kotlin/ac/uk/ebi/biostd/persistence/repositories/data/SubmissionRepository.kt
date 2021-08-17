package ac.uk.ebi.biostd.persistence.repositories.data

import ac.uk.ebi.biostd.persistence.common.model.BasicSubmission
import ac.uk.ebi.biostd.persistence.common.request.SubmissionFilter
import ac.uk.ebi.biostd.persistence.common.service.SubmissionQueryService
import ac.uk.ebi.biostd.persistence.common.exception.SubmissionNotFoundException
import ac.uk.ebi.biostd.persistence.filter.SubmissionFilterSpecification
import ac.uk.ebi.biostd.persistence.mapping.extended.to.DbToExtRequest
import ac.uk.ebi.biostd.persistence.mapping.extended.to.ToExtSubmissionMapper
import ac.uk.ebi.biostd.persistence.model.DbSubmission
import ac.uk.ebi.biostd.persistence.model.constants.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.pagination.OffsetPageRequest
import ac.uk.ebi.biostd.persistence.repositories.SectionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionRequestDataRepository
import ac.uk.ebi.biostd.persistence.repositories.SubmissionStatsDataRepository
import ac.uk.ebi.biostd.persistence.repositories.data.CollectionSqlDataService.Companion.SIMPLE_GRAPH
import ac.uk.ebi.biostd.persistence.repositories.data.CollectionSqlDataService.Companion.asBasicSubmission
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs
import ebi.ac.uk.extended.model.ExtSubmission
import mu.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Order
import org.springframework.transaction.annotation.Transactional
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.time.OffsetDateTime

private val logger = KotlinLogging.logger {}
private val defaultOrder = Order.asc("id")

@Suppress("TooManyFunctions")
internal open class SubmissionRepository(
    private val submissionRepository: SubmissionDataRepository,
    private val sectionRepository: SectionDataRepository,
    private val statsRepository: SubmissionStatsDataRepository,
    private val requestRepository: SubmissionRequestDataRepository,
    private val extSerializationService: ExtSerializationService,
    private var submissionMapper: ToExtSubmissionMapper
) : SubmissionQueryService {
    @Transactional(readOnly = true)
    override fun existByAccNo(accNo: String): Boolean = submissionRepository.existsByAccNo(accNo)

    override fun findExtByAccNo(accNo: String): ExtSubmission? =
        submissionRepository.getByAccNoAndVersionGreaterThan(accNo, 0)
            ?.let { submissionMapper.toExtSubmission(loadSubmissionAndStatus(accNo)) }

    @Transactional(readOnly = true)
    override fun getExtByAccNo(accNo: String) = submissionMapper.toExtSubmission(loadSubmissionAndStatus(accNo))

    @Transactional(readOnly = true)
    override fun getExtByAccNoAndVersion(accNo: String, version: Int) =
        submissionMapper.toExtSubmission(loadSubmissionAndStatus(accNo, version))

    @Transactional
    override fun expireSubmissions(accNumbers: List<String>) {
        submissionRepository.deleteSubmissions(accNumbers, OffsetDateTime.now())
    }

    @Transactional(readOnly = true)
    override fun getExtendedSubmissions(filter: SubmissionFilter): Page<Result<ExtSubmission>> {
        val filterSpecs = SubmissionFilterSpecification(filter)
        val pageable = OffsetPageRequest(filter.offset, filter.limit, Sort.by(defaultOrder))

        return submissionRepository
            .findAll(filterSpecs.specification, pageable, EntityGraphs.named(SIMPLE_GRAPH))
            .map { runCatching { getExtByAccNoAndVersion(it.accNo, it.version) } }
    }

    override fun getSubmissionsByUser(email: String, filter: SubmissionFilter): List<BasicSubmission> {
        val filterSpecs = SubmissionFilterSpecification(filter, email)
        val pageable = PageRequest.of(filter.pageNumber, filter.limit, Sort.by(SUB_RELEASE_TIME).descending())

        return submissionRepository
            .findAll(filterSpecs.specification, pageable, EntityGraphs.named(SIMPLE_GRAPH))
            .content
            .map { it.asBasicSubmission() }
    }

    override fun getRequest(accNo: String, version: Int): ExtSubmission {
        val request = requestRepository.getByAccNoAndVersion(accNo, version)
        return extSerializationService.deserialize(request.request)
    }

    private fun loadSubmissionAndStatus(accNo: String, version: Int? = null): DbToExtRequest =
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
