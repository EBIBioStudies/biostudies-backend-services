package ac.uk.ebi.biostd.persistence.doc.db.data

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import org.springframework.data.mongodb.core.MongoTemplate

class FileListDocFileDocDataRepository(
    private val fileListDocFileRepository: FileListDocFileRepository,
    private val mongoTemplate: MongoTemplate,
) : FileListDocFileRepository by fileListDocFileRepository {
}
