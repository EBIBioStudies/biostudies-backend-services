package ac.uk.ebi.biostd.submission.web.resources.ext.mapping

import ac.uk.ebi.biostd.submission.domain.extended.ExtFileListFilesRequest
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.WebExtPage
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

class ExtendedFilePageMapper(
    private val instanceBase: URI,
) {
    fun asExtPage(
        page: Page<ExtFile>,
        request: ExtFileListFilesRequest,
    ): WebExtPage<ExtFile> =
        WebExtPage(
            content = page.content,
            totalElements = page.totalElements,
            offset = page.pageable.offset,
            limit = page.pageable.pageSize,
            next = getNext(page, request),
            previous = getPrevious(page, request),
        )

    private fun getPrevious(
        page: Page<ExtFile>,
        request: ExtFileListFilesRequest,
    ): String? = if (page.hasPrevious()) instanceBase.resolve(asUrl(page.previousPageable(), request)).toString() else null

    private fun getNext(
        page: Page<ExtFile>,
        request: ExtFileListFilesRequest,
    ): String? = if (page.hasNext()) instanceBase.resolve(asUrl(page.nextPageable(), request)).toString() else null

    private fun asUrl(
        pageable: Pageable,
        request: ExtFileListFilesRequest,
    ): String =
        UriComponentsBuilder
            .fromUriString("$instanceBase/submissions/extended/${request.accNo}/referencedFiles-page/${request.fileListPath}")
            .pathSegment()
            .queryParam("offset", pageable.offset)
            .queryParam("limit", pageable.pageSize)
            .build()
            .toUriString()
}
