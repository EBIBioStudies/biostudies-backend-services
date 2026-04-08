package ac.uk.ebi.biostd.submission.web.resources.ext.mapping

import ac.uk.ebi.biostd.submission.domain.extended.ExtLinkListFilesRequest
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.WebExtPage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ExtendedLinkPageMapper(
    private val instanceBase: URI,
) {
    fun asExtPage(
        page: Page<ExtLink>,
        request: ExtLinkListFilesRequest,
    ): WebExtPage<ExtLink> =
        WebExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page, request),
            previous = getPrevious(page, request),
        )

    private fun getPrevious(
        page: Page<ExtLink>,
        request: ExtLinkListFilesRequest,
    ): String? = if (page.hasPrevious()) instanceBase.resolve(asUrl(page.previousPageable(), request)).toString() else null

    private fun getNext(
        page: Page<ExtLink>,
        request: ExtLinkListFilesRequest,
    ): String? = if (page.hasNext()) instanceBase.resolve(asUrl(page.nextPageable(), request)).toString() else null

    private fun asUrl(
        pageable: Pageable,
        request: ExtLinkListFilesRequest,
    ): String =
        UriComponentsBuilder
            .fromUriString("$instanceBase/submissions/extended/${request.accNo}/linkList/${request.linkListName}")
            .pathSegment()
            .queryParam("offset", pageable.offset)
            .queryParam("limit", pageable.pageSize)
            .build()
            .toUriString()
}
