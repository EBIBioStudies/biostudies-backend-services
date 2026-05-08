package ac.uk.ebi.biostd.submission.pmc

import ac.uk.ebi.biostd.persistence.doc.model.PmcDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.PmcSubmissionStatus
import ac.uk.ebi.biostd.persistence.doc.service.DistributedLockService
import ac.uk.ebi.biostd.persistence.doc.service.PmcSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.FOUND_LINKS
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.NO_LINKS
import ac.uk.ebi.biostd.submission.pmc.LinksProcessingResult.ERROR
import ac.uk.ebi.biostd.submission.pmc.LinksProcessingResult.SUCCESS
import ac.uk.ebi.biostd.submission.pmc.PmcLinksProcessor.Companion.CHUNK_SIZE
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

data class PmcId(
    val accNo: String,
    val pmcId: String,
) {
    constructor(accNo: String) : this(accNo, accNo.replace("S-EPMC", "PMC"))
}

enum class LinksExtractionResult {
    FOUND_LINKS,
    NO_LINKS,
    ERROR,
    TIME_OUT,
    ;

    companion object {
        val SUCCESS = setOf(FOUND_LINKS, NO_LINKS)
    }
}

data class PmcLinksResult(
    val sub: ExtSubmission,
    val result: LinksExtractionResult,
)

enum class LinksProcessingResult {
    SUCCESS,
    ERROR,
    TIMEOUT,
}

data class ProcessingResult(
    val accNo: String,
    val version: Int,
    val result: LinksProcessingResult,
)

data class ProcessConfig(
    val limit: Int,
    val chunkSize: Int = CHUNK_SIZE,
    val waitSeconds: Int? = null,
)

private val logger = KotlinLogging.logger {}

class PmcLinksProcessor(
    val pmcSubmissionsService: PmcSubmissionQueryService,
    val submissionService: ExtSubmissionService,
    val securityService: SecurityQueryService,
    val pmcLinksLoader: PmcLinksLoader,
    val distributedLockService: DistributedLockService,
) {
    suspend fun loadFromDb(config: ProcessConfig): List<ProcessingResult> {
        val user = securityService.getUser(USER_EMAIL)
        return loadFromDb(config, user)
    }

    suspend fun loadFromDb(
        config: ProcessConfig,
        user: SecurityUser,
    ): List<ProcessingResult> =
        distributedLockService.onLockProcess(PMC_EXTRACT_LINKS, user.email) {
            loadDbSubmissions(user, config)
        }

    private suspend fun loadDbSubmissions(
        user: SecurityUser,
        config: ProcessConfig,
    ): List<ProcessingResult> {
        logger.info("Loading PMC links from DB config = $config")
        val index = AtomicInteger(1)
        val submissions =
            pmcSubmissionsService
                .findByStatus(PmcSubmissionStatus.SUBMITTED, config.limit)
                .chunked(config.chunkSize)
                .map { chunk ->
                    logger.info("Loading chunk ${index.getAndIncrement()} of ${chunk.size}")
                    loadDocSubmissions(config, user, chunk)
                }.toList()
        return submissions.flatten()
    }

    suspend fun loadSubmissions(
        config: ProcessConfig,
        user: SecurityUser,
        accNos: List<String>,
    ): List<ProcessingResult> {
        val submissions = pmcSubmissionsService.findByAccNos(accNos).toList()
        return loadDocSubmissions(config, user, submissions)
    }

    private suspend fun loadDocSubmissions(
        config: ProcessConfig,
        user: SecurityUser,
        submissions: List<PmcDocSubmission>,
    ): List<ProcessingResult> {
        val loadResult = pmcLinksLoader.loadSubmissionsLinks(config, submissions.map { it.accNo })
        logger.info { "Loaded ${loadResult.size} submissions links" }

        val processingResult = processSubmissions(user, loadResult)
        logger.info { "Processed ${processingResult.size} submissions" }

        val completed = submissions.filter { processingResult.get(it.accNo)?.result == LinksProcessingResult.SUCCESS }
        pmcSubmissionsService.updateStatus(completed, PmcSubmissionStatus.LINKS_EXTRACTED)

        val failed = submissions.filter { processingResult.get(it.accNo)?.result == LinksProcessingResult.ERROR }
        pmcSubmissionsService.updateStatus(failed, PmcSubmissionStatus.ERROR_LINKS_EXTRACTION)

        logger.info("PMC links loaded ${processingResult.values.groupingBy { it.result }.eachCount()}")
        return processingResult.values.toList()
    }

    private suspend fun processSubmissions(
        user: SecurityUser,
        loadResult: List<PmcLinksResult>,
    ): Map<String, ProcessingResult> {
        val updated = loadResult.filter { it.result == FOUND_LINKS }.map { it.sub }
        val submitted = submissionService.submitExt(user.email, updated, PER_SUBMISSION_WAIT_TIME)
        logger.info { "Submitted ${submitted.size} of ${updated.size} submissions correctly" }

        val subbmitedByAccNo = submitted.associateBy { it.accNo }

        val processed =
            submitted
                .plus(loadResult.filter { it.result == NO_LINKS }.map { it.sub })
                .map { ProcessingResult(it.accNo, it.version, SUCCESS) }
        val errors =
            updated
                .filter { subbmitedByAccNo.contains(it.accNo).not() }
                .map { ProcessingResult(it.accNo, it.version, ERROR) }

        return processed.plus(errors).associateBy { it.accNo }
    }

    companion object {
        val PER_SUBMISSION_WAIT_TIME = 25.seconds
        const val CHUNK_SIZE = 100
        const val USER_EMAIL = "biostudies-dev@ebi.ac.uk"
        const val PMC_EXTRACT_LINKS = "PMC_EXTRACT_LINKS"
    }
}
