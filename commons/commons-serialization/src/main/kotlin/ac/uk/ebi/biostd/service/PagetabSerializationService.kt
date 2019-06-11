package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.io.FilesSource
import ebi.ac.uk.model.Submission

internal class PagetabSerializationService(
    private val serializer: PagetabSerializer = PagetabSerializer(),
    private val libraryFileSerializer: LibraryFileSerializer = LibraryFileSerializer(serializer)
) : SerializationService {

    override fun <T> serializeElement(element: T, format: SubFormat) =
        serializer.serializeElement(element, format)

    override fun serializeSubmission(submission: Submission, format: SubFormat) =
        serializer.serializeSubmission(submission, format)

    override fun deserializeSubmission(content: String, format: SubFormat): Submission =
        serializer.deserializeSubmission(content, format)

    override fun deserializeSubmission(content: String, format: SubFormat, source: FilesSource): Submission =
        libraryFileSerializer.loadLibraryFiles(serializer.deserializeSubmission(content, format), format, source)
}
