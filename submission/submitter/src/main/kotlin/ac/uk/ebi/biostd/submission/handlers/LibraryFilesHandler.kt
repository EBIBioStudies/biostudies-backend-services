package ac.uk.ebi.biostd.submission.handlers

import ac.uk.ebi.biostd.SerializationService
import ac.uk.ebi.biostd.SubFormat
import ac.uk.ebi.biostd.submission.model.FilesSource
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.extensions.allLibraryFileSections

class LibraryFilesHandler(private val serializationService: SerializationService) {
    fun processLibraryFiles(submission: ExtendedSubmission, filesSource: FilesSource, format: SubFormat) {
        submission.allLibraryFileSections().forEach { section ->
            val libFileContent = filesSource.readText(section.libraryFile!!.name)
            val filesTable = serializationService.deserializeElement<FilesTable>(libFileContent, format)

            filesTable.elements.forEach { section.addReferencedFile(it) }
        }
    }
}
