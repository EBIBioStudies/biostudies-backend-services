package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.libraryFileName

internal class LibraryFileSerializer(private val serializer: PagetabSerializer) {
    internal fun deserializeLibraryFile(submission: Submission, format: SubFormat, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.libraryFileName != null }
            .map { section -> section to section.libraryFileName!! }
            .forEach { (section, libraryFile) -> section.libraryFile = getLibraryFile(libraryFile, format, source) }
        return submission
    }

    private fun getLibraryFile(libraryFile: String, format: SubFormat, source: FilesSource): LibraryFile {
        val fileContent = source.getFile(libraryFile).readText()
        val filesTable = serializer.deserializeElement<FilesTable>(fileContent, format)
        return LibraryFile(libraryFile, filesTable.elements)
    }
}
