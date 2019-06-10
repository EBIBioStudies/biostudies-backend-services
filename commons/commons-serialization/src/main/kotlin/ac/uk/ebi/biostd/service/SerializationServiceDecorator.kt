package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.ISerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.libraryFileName

internal class SerializationServiceDecorator(
    private val serializationService: SerializationService = SerializationService()
) : ISerializationService {

    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializationService.serializeElement(element, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializationService.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission {
        return serializationService.deserializeSubmission(content, format)
    }

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission {
        val submission = serializationService.deserializeSubmission(content, format)
        submission.allSections()
            .filter { section -> section.libraryFileName != null }
            .map { section -> section to section.libraryFileName!! }
            .forEach { (section, libraryFile) -> section.libraryFile = getLibraryFile(libraryFile, format, source) }
        return submission
    }

    private fun getLibraryFile(libraryFile: String, format: SubFormat, source: FilesSource): LibraryFile {
        val fileContent = source.getFile(libraryFile).readText()
        val filesTable = serializationService.deserializeElement<FilesTable>(fileContent, format)
        return LibraryFile(libraryFile, filesTable.elements)
    }
}
