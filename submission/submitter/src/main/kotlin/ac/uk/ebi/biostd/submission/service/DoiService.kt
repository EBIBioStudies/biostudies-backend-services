package ac.uk.ebi.biostd.submission.service

import ac.uk.ebi.biostd.common.properties.DoiProperties
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.InvalidAuthorNameException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgException
import ac.uk.ebi.biostd.submission.exceptions.InvalidOrgNamesException
import ac.uk.ebi.biostd.submission.exceptions.MissingAuthorAffiliationException
import ac.uk.ebi.biostd.submission.exceptions.MissingDoiFieldException
import ac.uk.ebi.biostd.submission.exceptions.MissingTitleException
import ac.uk.ebi.biostd.submission.model.Contributor
import ac.uk.ebi.biostd.submission.model.DoiRequest
import ebi.ac.uk.commons.http.ext.RequestParams
import ebi.ac.uk.commons.http.ext.post
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allSections
import ebi.ac.uk.extended.model.computedTitle
import ebi.ac.uk.io.FileUtils
import ebi.ac.uk.util.collections.ifNotEmpty
import mu.KotlinLogging
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.nio.file.Files

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

private val logger = KotlinLogging.logger {}

@Suppress("ThrowsCount")
class DoiService(
    private val webClient: WebClient,
    private val properties: DoiProperties,
) {
    fun registerDoi(sub: ExtSubmission) {
        val title = requireNotNull(sub.computedTitle) { throw MissingTitleException() }
        val request = DoiRequest(sub.accNo, title, properties.uiUrl, getContributors(sub))
        val requestFile = Files.createTempFile("${TEMP_FILE_NAME}_${sub.accNo}", ".xml").toFile()
        FileUtils.writeContent(requestFile, request.asXmlRequest())

        val body = LinkedMultiValueMap<String, Any>().apply {
            add(USER_PARAM, properties.user)
            add(PASSWORD_PARAM, properties.password)
            add(OPERATION_PARAM, OPERATION_PARAM_VALUE)
            add(FILE_PARAM, FileSystemResource(requestFile))
        }

        logger.info { "${sub.accNo} ${sub.owner} Registering DOI" }
        webClient.post(properties.endpoint, RequestParams(body = body))
    }

    private fun getContributors(sub: ExtSubmission): List<Contributor> {
        val organizations = getOrganizations(sub)
        return sub.allSections
            .filter { it.type.lowercase() == AUTHOR_TYPE }
            .ifEmpty { throw MissingDoiFieldException(AUTHOR_TYPE) }
            .map { it.asContributor(organizations) }
    }

    private fun ExtSection.asContributor(orgs: Map<String, String>): Contributor {
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

    private fun getOrganizations(sub: ExtSubmission): Map<String, String> {
        val organizations = sub.allSections
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
}
