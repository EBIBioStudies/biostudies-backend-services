package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.integration.SubFormat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName

internal class FileListSerializer(private val serializer: PagetabSerializer) {
    internal fun deserializeFileList(submission: Submission, format: SubFormat, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, format, source) }
        return submission
    }

    private fun getFileList(fileList: String, format: SubFormat, source: FilesSource): FileList {
        val fileContent = source.getFile(fileList).readText()
        val filesTable = serializer.deserializeElement<FilesTable>(fileContent, format)

        return FileList(fileList, filesTable.elements)
    }
}
