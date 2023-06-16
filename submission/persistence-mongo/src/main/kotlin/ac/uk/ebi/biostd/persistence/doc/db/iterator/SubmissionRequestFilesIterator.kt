package ac.uk.ebi.biostd.persistence.doc.db.iterator

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import org.springframework.data.domain.PageRequest

private const val CHUNK_SIZE = 10

fun SubmissionRequestFilesRepository.getRequestFilesAsSequence(
    accNo: String,
    version: Int,
    startingAt: Int,
): Sequence<DocSubmissionRequestFile> = sequence {
    var startPage = 0
    var files = findRequestFiles(accNo, version, startingAt, PageRequest.of(startPage, CHUNK_SIZE))

    while (files.isEmpty.not()) {
        yieldAll(files)
        files = findRequestFiles(accNo, version, startingAt, PageRequest.of(++startPage, CHUNK_SIZE))
    }
}
