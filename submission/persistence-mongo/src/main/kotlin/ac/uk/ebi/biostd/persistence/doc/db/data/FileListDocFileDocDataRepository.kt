package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.reactive.repositories.FileListDocFileRepository

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
) : FileListDocFileRepository by fileListDocFileRepository
