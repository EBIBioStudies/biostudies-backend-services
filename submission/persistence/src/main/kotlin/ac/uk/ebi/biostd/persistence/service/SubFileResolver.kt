package ac.uk.ebi.biostd.persistence.service

import ac.uk.ebi.biostd.persistence.integration.PersistenceProperties
import ebi.ac.uk.extended.integration.FilesSource
import java.nio.file.Path

class SubFileResolver(private val persistenceProperties: PersistenceProperties) {

    fun getSource(secret: String): FilesSource = TODO()

    fun getSubmissionFolder(secret: String): Path = TODO()
}
