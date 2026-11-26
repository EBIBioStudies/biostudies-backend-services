package ac.uk.ebi.biostd.client.extensions

import ac.uk.ebi.biostd.client.dto.ExtSubPageQuery
import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import uk.ac.ebi.biostd.base.isNotBlank
import uk.ac.ebi.biostd.extended.model.ExtPage
import uk.ac.ebi.biostd.extended.model.ExtSubmission

fun BioWebClient.getExtSubmissionsAsSequence(extPageQuery: ExtSubPageQuery): Sequence<ExtSubmission> =
    pageIterator(extPageQuery)
        .asSequence()
        .map { it.content }
        .flatten()

private fun BioWebClient.pageIterator(query: ExtSubPageQuery): Iterator<ExtPage> =
    object : Iterator<ExtPage> {
        var firstPage: ExtPage? = getExtSubmissions(query)
        var currentPage = firstPage!!

        override fun hasNext(): Boolean = firstPage != null || currentPage.next.isNotBlank()

        override fun next(): ExtPage {
            require(hasNext()) { throw NoSuchElementException() }

            when (firstPage) {
                null -> currentPage = getExtSubmissionsPage(currentPage.next!!)
                else -> firstPage = null
            }

            return currentPage
        }
    }
