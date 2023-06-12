package ac.uk.ebi.biostd.persistence.doc.db.iterator

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

fun SubmissionRequestFilesRepository.getRequestFilesAsSequence(
    accNo: String,
    version: Int,
    startingAt: Int,
): Sequence<DocSubmissionRequestFile> {
    return pageIterator(accNo, version, startingAt)
        .asSequence()
        .map { it.content }
        .flatten()
}

private const val CHUNK_SIZE = 10

private fun SubmissionRequestFilesRepository.pageIterator(
    accNo: String,
    version: Int,
    startingAt: Int,
): Iterator<Page<DocSubmissionRequestFile>> = object : Iterator<Page<DocSubmissionRequestFile>> {
    var next = 0
    var currentPage: Page<DocSubmissionRequestFile> = Page.empty()

    override fun hasNext(): Boolean = if (next == 0) true else currentPage.hasNext()

    override fun next(): Page<DocSubmissionRequestFile> {
        if (!hasNext()) throw NoSuchElementException()

        currentPage = findRequestFiles(accNo, version, startingAt, PageRequest.of(next, CHUNK_SIZE))
        next++

        return currentPage
    }
}
