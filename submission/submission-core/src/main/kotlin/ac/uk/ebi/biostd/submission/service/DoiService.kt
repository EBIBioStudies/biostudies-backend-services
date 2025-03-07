package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorNameException
import ac.uk.ebi.biostd.submission.exceptions.InvalidDoiException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNameException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import ac.uk.ebi.biostd.submission.exceptions.RemovedDoiException
import ac.uk.ebi.biostd.submission.model.Contributor
import ac.uk.ebi.biostd.submission.model.DoiRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.base.isNotBlank
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.commons.http.builder.httpHeadersOf
import ebi.ac.uk.commons.http.builder.linkedMultiValueMapOf
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.DOI
import ebi.ac.uk.model.constants.SubFields.TITLE
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.isAuthor
import ebi.ac.uk.model.extensions.isOrganization
import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.web.reactive.function.client.WebClient
import java.nio.file.Files
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Suppress("ThrowsCount", "ReturnCount")
class DoiService(
    private val webClient: WebClient,
    private val properties: DoiProperties,
) {
    fun calculateDoi(
        accNo: String,
        rqt: SubmitRequest,
    ): String? {
        val doi = rqt.submission.attributes.find { it.name == DOI.name } ?: return null
        val previousDoi = rqt.previousVersion?.doi

        if (previousDoi != null) {
            val value = doi.value
            requireNotNull(value) { throw RemovedDoiException(previousDoi) }
            require(value == previousDoi) { throw InvalidDoiException(value, previousDoi) }
            return previousDoi
        }

        return registerDoi(accNo, rqt)
    }

    private fun registerDoi(
        accNo: String,
        rqt: SubmitRequest,
    ): String {
        val sub = rqt.submission
        val timestamp = Instant.now().epochSecond.toString()
        val title = rqt.submission.find(TITLE) ?: rqt.submission.section.find(TITLE) ?: throw MissingTitleException()
        val request = DoiRequest(accNo, title, properties.email, timestamp, properties.uiUrl, getContributors(sub))
        val requestFile = Files.createTempFile("${TEMP_FILE_NAME}_$accNo", ".xml").toFile()
        val xmlRequest = request.asXmlRequest()
        FileUtils.writeContent(requestFile, xmlRequest)

        val headers = httpHeadersOf(CONTENT_TYPE to MULTIPART_FORM_DATA)
        val body =
            linkedMultiValueMapOf(
                USER_PARAM to properties.user,
                PASSWORD_PARAM to properties.password,
                OPERATION_PARAM to OPERATION_PARAM_VALUE,
                FILE_PARAM to FileSystemResource(requestFile),
            )

        webClient.post(properties.endpoint, RequestParams(headers, body))
        logger.info { "$accNo ${rqt.owner} Registered DOI: '${request.doi}' with request:\n $xmlRequest" }

        return request.doi
    }

    private fun getContributors(submission: Submission): List<Contributor> {
        fun isNameValid(author: Section): Boolean = author.findAttr(NAME_ATTR).isNotBlank()

        val organizations = getOrganizations(submission)
        val contributors =
            submission
                .allSections()
                .filter { it.isAuthor() }
                .filter { isNameValid(it) }

        return contributors.map { it.asContributor(organizations) }
    }

    private fun Section.asContributor(organizations: Map<String, String>): Contributor {
        val names = findAttr(NAME_ATTR)?.trim() ?: throw InvalidAuthorNameException()
        val affiliation = findAttr(AFFILIATION_ATTR) ?: throw MissingAuthorAffiliationException()
        val org = organizations[affiliation] ?: throw InvalidAuthorAffiliationException(names, affiliation)

        return Contributor(
            name = names.substringBeforeLast(" ", "").nullIfBlank(),
            surname = names.substringAfterLast(" "),
            affiliation = org,
            orcid = find(ORCID_ATTR),
        )
    }

    private fun getOrganizations(submission: Submission): Map<String, String> {
        val organizations =
            submission
                .allSections()
                .filter { it.isOrganization() }
        validateOrganizations(organizations)

        return organizations.associateBy({ it.accNo!! }, { it.findAttr(NAME_ATTR)!! })
    }

    private fun validateOrganizations(organizations: List<Section>) {
        fun validate(org: Section) {
            val accNo = org.accNo
            requireNotNull(accNo) { throw InvalidOrgException() }
            requireNotNull(org.find(NAME_ATTR)) { throw InvalidOrgNameException(accNo) }
        }

        organizations
            .ifEmpty { throw MissingDoiFieldException(ORG_TYPE) }
            .forEach(::validate)
    }

    private fun Section.findAttr(attribute: String): String? = attributes.find { it.name.lowercase() == attribute.lowercase() }?.value

    companion object {
        internal const val AFFILIATION_ATTR = "Affiliation"
        internal const val NAME_ATTR = "Name"
        internal const val ORCID_ATTR = "ORCID"
        internal const val ORG_TYPE = "Organization"

        internal const val FILE_PARAM = "fname"
        internal const val OPERATION_PARAM = "operation"
        internal const val OPERATION_PARAM_VALUE = "doMDUpload"
        internal const val PASSWORD_PARAM = "login_passwd"
        internal const val USER_PARAM = "login_id"
        internal const val TEMP_FILE_NAME = "doi-request"
    }
}
