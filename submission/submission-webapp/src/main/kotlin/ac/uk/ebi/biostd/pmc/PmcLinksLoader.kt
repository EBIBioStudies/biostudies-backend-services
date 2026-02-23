package ac.uk.ebi.biostd.pmc

import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionQueryService
import ac.uk.ebi.biostd.submission.domain.extended.ExtSubmissionService
import ebi.ac.uk.base.Either
import ebi.ac.uk.client.pmc.PmcAnalisisRequest
import ebi.ac.uk.client.pmc.PmcClient
import ebi.ac.uk.client.pmc.PmcFile
import ebi.ac.uk.client.pmc.PmcFileLink
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allSectionsFiles
import ebi.ac.uk.io.KFiles
import ebi.ac.uk.security.integration.model.api.SecurityUser
import ebi.ac.uk.util.collections.indexOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import mu.KotlinLogging
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.net.URI
import kotlin.io.path.outputStream
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class PmcId(val accNo: String, val pmcId: String)

private val logger = KotlinLogging.logger {}

class PmcLinksLoader(
    val pmcWebClient: PmcClient,
    val queryService: ExtSubmissionQueryService,
    val serializationService: ExtSerializationService,
    val submissionService: ExtSubmissionService,
) {
    suspend fun loadSubmission(user: SecurityUser, accNo: String) {
        val pmcId = PmcId(accNo, accNo.replace("S-EPMC", "PMC"))
        val submission = queryService.getExtendedSubmission(accNo, includeFileListFiles = true)
        if (triggerAnalisis(submission, pmcId)) {
            waitForResult(pmcId)
            val sections = submission.section.sections.toMutableList()
            val toReplace = sections.indexOf { it.fold({ it.type == LINKS_SECTION_TYPE }, { false }) }
            when (toReplace) {
                null -> sections.add(Either.left(createSection(pmcId)))
                else -> sections.set(toReplace, Either.left(createSection(pmcId)))
            }
            val newVersion = submission.section.copy(sections = sections)
            logger.info { "Submitting new version of submission ${pmcId.accNo} with extracted links" }
            submissionService.submitExt(user.email, submission.copy(section = newVersion))
        }
    }

    private suspend fun createSection(pmcId: PmcId): ExtSection {
        return ExtSection(
            type = LINKS_SECTION_TYPE,
            linkList = createLinkList(pmcId),
        )
    }

    private suspend fun createLinkList(pmcId: PmcId): ExtLinkList? {
        fun asLink(filename: String, pmcLinkLink: PmcFileLink): List<ExtLink> {
            return pmcLinkLink.tags
                .map {
                    ExtLink(
                        url = it.uri,
                        attributes = listOf(
                            ExtAttribute("filename", filename),
                            ExtAttribute("value", pmcLinkLink.exact),
                            ExtAttribute("type", pmcLinkLink.type),
                        )
                    )
                }
        }

        val links = pmcWebClient.getStatus(pmcId.pmcId).files
            .filter { it.status == "success" }
            .map { file -> pmcWebClient.getResult(pmcId.pmcId, file.filename) }
            .flatMap { result -> result.results.flatMap { asLink(result.filename, it) } }

        return when {
            links.isEmpty() -> null
            else -> {
                val linkList = KFiles.createTempFile("pmc-$pmcId", ".pmc")
                serializationService.serializeLinks(links.asFlow(), linkList.outputStream())
                ExtLinkList(filePath = "${pmcId.accNo}_extracted_links", linkList.toFile())
            }
        }
    }

    /**
     * Wait for PMC result, if any file succedd
     */
    private suspend fun waitForResult(pmcId: PmcId): Boolean {
        val result = withTimeoutOrNull(5.minutes) {
            while (isActive) {
                val results = pmcWebClient.getStatus(pmcId.pmcId).files.map { it.status }
                logger.info("Got result $results for ${pmcId.pmcId}")
                if (results.none { it == "pending" }) break else delay(1.seconds)
            }
            true
        }
        return result ?: false
    }

    private suspend fun triggerAnalisis(sub: ExtSubmission, pmcId: PmcId): Boolean {
        val files = sub.allSectionsFiles
            .filter { it.fileName.matches(Regex(".*\\.(doc|docx|txt|pdf|html)")) }
            .map {
                PmcFile(
                    filename = it.fileName,
                    url = "https://www.ebi.ac.uk/biostudies/files/${pmcId.accNo}/${it.fileName}"
                )
            }
        if (files.isNotEmpty()) {
            val request = PmcAnalisisRequest(
                pmcId = pmcId.pmcId,
                callback = URI.create("http://wp-np2-e9.ebi.ac.uk:9018/biostudiesWrapper/callback/${pmcId.pmcId}"),
                files = files
            )
            pmcWebClient.submitStudy(request)
            return true
        }

        logger.info("No files with required extensions found for submission ${sub.accNo}")
        return false
    }

    companion object {
        const val LINKS_SECTION_TYPE = "ExtractedLinks"
    }
}