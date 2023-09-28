package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository {

    suspend fun deleteAllFiles() {
        fileListDocFileRepository.deleteAll().awaitSingleOrNull()
    }

    suspend fun saveFile(fileListDocFile: FileListDocFile) {
        fileListDocFileRepository.save(fileListDocFile).awaitSingle()
    }
}
