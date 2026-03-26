package ac.uk.ebi.biostd.submission.pmc

import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.FOUND_LINKS
import ac.uk.ebi.biostd.submission.pmc.LinksExtractionResult.NO_LINKS
import ebi.ac.uk.base.Either
import ebi.ac.uk.client.pmc.PmcAnalisisRequest
import ebi.ac.uk.client.pmc.PmcClient
import ebi.ac.uk.client.pmc.PmcFile
import ebi.ac.uk.client.pmc.PmcFileLink
import ebi.ac.uk.client.pmc.SubmissionResult.ANALISIS_IN_PROGRESS
import ebi.ac.uk.client.pmc.SubmissionResult.OK
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.util.collections.indexOf
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.net.URI
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

class PmcLinksLoader(
    val pmcWebClient: PmcClient,
    val queryService: ExtSubmissionQueryService,
    val serializationService: ExtSerializationService,
) {
    suspend fun loadSubmissionsLinks(
        config: ProcessConfig,
        accNoList: List<String>,
    ): List<PmcLinksResult> =
        coroutineScope {
            suspend fun loadLinks(submission: ExtSubmission): PmcLinksResult {
                val pmcId = PmcId(submission.accNo)
                val linksGenerated = generateLinks(config, submission, pmcId)
                return when {
                    linksGenerated -> PmcLinksResult(attachLinks(pmcId, submission), FOUND_LINKS)
                    else -> PmcLinksResult(submission, NO_LINKS)
                }
            }

            @Suppress("TooGenericExceptionCaught")
            suspend fun loadLinksSafely(submission: ExtSubmission): PmcLinksResult =
                try {
                    loadLinks(submission)
                } catch (e: TimeoutCancellationException) {
                    logger.error("Error loading links for submission ${submission.accNo}", e)
                    PmcLinksResult(submission, LinksExtractionResult.TIME_OUT)
                } catch (e: RuntimeException) {
                    logger.error("Error loading links for submission ${submission.accNo}", e)
                    PmcLinksResult(submission, LinksExtractionResult.ERROR)
                }

            suspend fun loadSubmission(accNo: String): PmcLinksResult? {
                logger.info("Loading PMC links of submission $accNo")
                val submission = queryService.findExtendedSubmission(accNo, includeFileListFiles = true)
                return submission?.let { loadLinksSafely(submission) }
            }

            return@coroutineScope accNoList
                .map { async { loadSubmission(it) } }
                .awaitAll()
                .filterNotNull()
        }

    private suspend fun attachLinks(
        pmcId: PmcId,
        submission: ExtSubmission,
    ): ExtSubmission {
        val sections = submission.section.sections.toMutableList()
        val toReplace = sections.indexOf { it.fold({ it.type == LINKS_SECTION_TYPE }, { false }) }
        when (toReplace) {
            null -> sections.add(Either.left(createSection(pmcId)))
            else -> sections.set(toReplace, Either.left(createSection(pmcId)))
        }
        val newVersion = submission.section.copy(sections = sections)
        return submission.copy(section = newVersion)
    }

    private suspend fun createSection(pmcId: PmcId): ExtSection =
        ExtSection(
            type = LINKS_SECTION_TYPE,
            linkList = createLinkList(pmcId),
        )

    private suspend fun createLinkList(pmcId: PmcId): ExtLinkList? {
        fun asLink(
            filename: String,
            pmcLinkLink: PmcFileLink,
        ): List<ExtLink> =
            pmcLinkLink.tags
                .map {
                    ExtLink(
                        url = it.uri,
                        attributes =
                            listOf(
                                ExtAttribute("filename", filename),
                                ExtAttribute("value", pmcLinkLink.exact),
                                ExtAttribute("type", pmcLinkLink.type),
                            ),
                    )
                }

        val links =
            pmcWebClient
                .getStatus(pmcId.pmcId)
                .files
                .filter { it.status == "success" }
                .map { file -> pmcWebClient.getResult(pmcId.pmcId, file.filename) }
                .flatMap { result -> result.results.flatMap { asLink(result.filename, it) } }

        return when {
            links.isEmpty() -> {
                null
            }

            else -> {
                val linkList = KFiles.createTempFile("pmc-$pmcId", ".pmc")
                serializationService.serializeLinks(links.asFlow(), linkList.outputStream())
                ExtLinkList(filePath = "${pmcId.accNo}_extracted_links", linkList.toFile())
            }
        }
    }

    private suspend fun generateLinks(
        config: ProcessConfig,
        submission: ExtSubmission,
        pmcId: PmcId,
    ): Boolean {
        suspend fun waitForResult() {
            withTimeout((config.waitSeconds ?: WAIT_SECONDS).seconds) {
                var results = pmcWebClient.getStatus(pmcId.pmcId).files.map { it.status }
                while (isActive && results.any { it == "pending" }) {
                    results = pmcWebClient.getStatus(pmcId.pmcId).files.map { it.status }
                    logger.info("Got result $results for pmcId='${pmcId.pmcId}' accNo='${pmcId.accNo}' ")
                    delay(CHECK_INTERVAL_SECONDS.seconds)
                }
            }
        }

        suspend fun getResults(): Boolean {
            val hasLinks = triggerAnalisis(submission, pmcId)
            if (hasLinks) waitForResult()
            return hasLinks
        }

        return when {
            hasAnalisis(pmcId) -> true
            else -> getResults()
        }
    }

    private suspend fun hasAnalisis(pmcId: PmcId): Boolean = pmcWebClient.findStatus(pmcId.pmcId) != null

    private suspend fun triggerAnalisis(
        sub: ExtSubmission,
        pmcId: PmcId,
    ): Boolean {
        val files =
            sub.allSectionsFiles
                .filter { it.fileName.matches(Regex(".*\\.(doc|docx|txt|pdf|html)")) }
                .map {
                    PmcFile(
                        filename = it.fileName,
                        url = "https://www.ebi.ac.uk/biostudies/files/${pmcId.accNo}/${it.fileName}",
                    )
                }
        if (files.isNotEmpty()) {
            val request =
                PmcAnalisisRequest(
                    pmcId = pmcId.pmcId,
                    callback = URI.create("http://wp-np2-e9.ebi.ac.uk:9018/biostudiesWrapper/callback/${pmcId.pmcId}"),
                    files = files,
                )
            val result = pmcWebClient.submitStudy(request)
            return result == ANALISIS_IN_PROGRESS || result == OK
        }

        logger.info("No files with required extensions found for submission ${sub.accNo}")
        return false
    }

    companion object {
        const val LINKS_SECTION_TYPE = "ExtractedLinks"
        const val WAIT_SECONDS = 5
        const val CHECK_INTERVAL_SECONDS = 10
        const val CHUNK_SIZE = 100
    }
}
