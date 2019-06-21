package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceProperties
import ebi.ac.uk.utils.FilesSource
import java.nio.file.Path

class SubFileResolver(private val persistenceProperties: PersistenceProperties) {

    fun getSubmissionSource(secret: String): FilesSource = TODO()

    fun getSubmissionFolder(secret: String): Path = TODO()
}
