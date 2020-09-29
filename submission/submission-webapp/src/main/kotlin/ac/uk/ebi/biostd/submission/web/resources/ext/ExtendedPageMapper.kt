package ac.uk.ebi.biostd.submission.web.resources.ext

import ac.uk.ebi.biostd.submission.web.model.ExtPage
import ebi.ac.uk.extended.model.ExtSubmission
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.net.URI

class ExtendedPageMapper(private val instanceBase: URI) {

    fun asExtPage(page: Page<ExtSubmission>): ExtPage =
            ExtPage(page.content, page.totalPages, getNextPage(page), getPreviousPage(page))

    private fun getPreviousPage(page: Page<ExtSubmission>): String? {
        return when (page.hasPrevious()) {
            true -> instanceBase.resolve(asQueryParams(page.previousPageable())).toString()
            false -> null
        }
    }

    private fun getNextPage(page: Page<ExtSubmission>): String? {
        return when (page.hasNext()) {
            true -> instanceBase.resolve(asQueryParams(page.nextPageable())).toString()
            false -> null
        }
    }

    private fun asQueryParams(nextPageable: Pageable): String =
        "$instanceBase/submissions/extended?page=${nextPageable.pageNumber}&${nextPageable.pageSize}"
}
