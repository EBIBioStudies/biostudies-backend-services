package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorNameException
import ac.uk.ebi.biostd.submission.exceptions.InvalidDoiException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNamesException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import ac.uk.ebi.biostd.submission.model.Contributor
import ac.uk.ebi.biostd.submission.model.DoiRequest
import ac.uk.ebi.biostd.submission.model.SubmitRequest
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.SubFields.DOI
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.title
import ebi.ac.uk.util.collections.ifNotEmpty
import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

@Suppress("ThrowsCount", "ReturnCount")
class DoiService(
    private val webClient: WebClient,
    private val properties: DoiProperties,
) {
    fun calculateDoi(accNo: String, rqt: SubmitRequest): String? {
        val doi = rqt.submission.attributes.find { it.name == DOI.name } ?: return null
        val previousDoi = rqt.previousVersion?.doi

        if (previousDoi != null) {
            require(doi.value == previousDoi) { throw InvalidDoiException() }
            return previousDoi
        }

        return registerDoi(accNo, rqt)
    }

    private fun registerDoi(accNo: String, rqt: SubmitRequest): String {
        val sub = rqt.submission
        val title = requireNotNull(sub.title) { throw MissingTitleException() }
        val request = DoiRequest(accNo, title, properties.uiUrl, getContributors(sub))
        val requestFile = Files.createTempFile("${TEMP_FILE_NAME}_$accNo", ".xml").toFile()
        FileUtils.writeContent(requestFile, request.asXmlRequest())

        val headers = HttpHeaders().apply { contentType = MULTIPART_FORM_DATA }
        val body = LinkedMultiValueMap<String, Any>().apply {
            add(USER_PARAM, properties.user)
            add(PASSWORD_PARAM, properties.password)
            add(OPERATION_PARAM, OPERATION_PARAM_VALUE)
            add(FILE_PARAM, FileSystemResource(requestFile))
        }

        webClient.post(properties.endpoint, RequestParams(headers, body))
        logger.info { "$accNo ${rqt.owner} Registered DOI: '${request.doi}'" }

        return request.doi
    }

    private fun getContributors(sub: Submission): List<Contributor> {
        val organizations = getOrganizations(sub)
        return sub.allSections()
            .filter { it.type.lowercase() == AUTHOR_TYPE }
            .ifEmpty { throw MissingDoiFieldException(AUTHOR_TYPE) }
            .map { it.asContributor(organizations) }
    }

    private fun Section.asContributor(orgs: Map<String, String>): Contributor {
        val attrsMap = attributes.associateBy({ it.name.lowercase() }, { it.value })
        val names = requireNotNull(attrsMap[NAME_ATTR]) { throw InvalidAuthorNameException() }
        val affiliation = requireNotNull(attrsMap[AFFILIATION_ATTR]) { throw MissingAuthorAffiliationException() }
        val org = requireNotNull(orgs[affiliation]) { throw InvalidAuthorAffiliationException(names, affiliation) }

        return Contributor(
            name = names.substringBeforeLast(" ", ""),
            surname = names.substringAfterLast(" "),
            affiliation = org,
            orcid = attrsMap[ORCID_ATTR]
        )
    }

    private fun getOrganizations(sub: Submission): Map<String, String> {
        val organizations = sub.allSections()
            .filter { it.type.lowercase() == ORG_TYPE }
            .ifEmpty { throw MissingDoiFieldException(ORG_TYPE) }
            .associateBy(
                { requireNotNull(it.accNo) { throw InvalidOrgException() } },
                { org -> org.attributes.find { it.name.lowercase() == NAME_ATTR }?.value.orEmpty() },
            )

        organizations.entries
            .filter { it.value.isEmpty() }
            .ifNotEmpty { entries -> throw InvalidOrgNamesException(entries.map { it.key }) }

        return organizations
    }

    companion object {
        internal const val AFFILIATION_ATTR = "affiliation"
        internal const val NAME_ATTR = "name"
        internal const val ORCID_ATTR = "orcid"

        internal const val ORG_TYPE = "organization"
        internal const val AUTHOR_TYPE = "author"

        internal const val FILE_PARAM = "fname"
        internal const val OPERATION_PARAM = "operation"
        internal const val OPERATION_PARAM_VALUE = "doMDUpload"
        internal const val PASSWORD_PARAM = "login_password"
        internal const val USER_PARAM = "login_id"
        internal const val TEMP_FILE_NAME = "doi-request"
    }
}
