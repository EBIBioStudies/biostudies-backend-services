package ac.uk.ebi.biostd.submission.pmc

import ac.uk.ebi.biostd.persistence.doc.model.PmcDocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.PmcSubmissionStatus
import ac.uk.ebi.biostd.persistence.doc.service.DistributedLockService
import ac.uk.ebi.biostd.persistence.doc.service.PmcSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.FOUND_LINKS
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.NO_LINKS
import ebi.ac.uk.coroutines.chunked
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.security.integration.components.SecurityQueryService
import ebi.ac.uk.security.integration.model.api.SecurityUser
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

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

data class ProcessingResult(
    val accNo: String,
    val version: Int,
    val result: LinksExtractionResult,
)

data class ProcessConfig(
    val limit: Int,
    val chunkSize: Int? = null,
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
        val submissions =
            pmcSubmissionsService
                .findByStatus(PmcSubmissionStatus.SUBMITTED, config.limit)
                .chunked(config.chunkSize ?: CHUNK_SIZE)
                .map { chunk -> loadDocSubmissions(config, user, chunk) }
                .toList()
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

        val updated = loadResult.filter { it.result == FOUND_LINKS }.map { it.sub }
        if (updated.isNotEmpty()) submissionService.submitExt(user.email, updated)
        logger.info { "Submitted ${updated.size} submissions" }

        val loadResultByAccNo = loadResult.associate { it.sub.accNo to it.result }
        val completed =
            submissions.filter { loadResultByAccNo[it.accNo] == FOUND_LINKS || loadResultByAccNo[it.accNo] == NO_LINKS }
        pmcSubmissionsService.updateStatus(completed, PmcSubmissionStatus.LINKS_EXTRACTED)

        val result = loadResult.map { ProcessingResult(it.sub.accNo, it.sub.version, it.result) }
        logger.info("PMC links loaded $result")
        return result
    }

    companion object {
        const val CHUNK_SIZE = 100
        const val USER_EMAIL = "biostudies-dev@ebi.ac.uk"
        const val PMC_EXTRACT_LINKS = "PMC_EXTRACT_LINKS"
    }
}
