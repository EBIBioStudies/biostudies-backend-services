package ac.uk.ebi.biostd.persistence.doc.db.iterator

import ac.uk.ebi.biostd.persistence.doc.db.repositories.SubmissionRequestFilesRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.springframework.data.domain.PageRequest

private const val CHUNK_SIZE = 10

fun SubmissionRequestFilesRepository.getRequestFilesAsFlow(
    accNo: String,
    version: Int,
    startingAt: Int,
): Flow<DocSubmissionRequestFile> = flow {
    var startPage = 0
    var files = findRequestFiles(accNo, version, startingAt, PageRequest.of(startPage, CHUNK_SIZE))

    while (files.isEmpty.not()) {
        emitAll(files.asFlow())
        files = findRequestFiles(accNo, version, startingAt, PageRequest.of(++startPage, CHUNK_SIZE))
    }
}
