package ac.uk.ebi.biostd.service

import ac.uk.ebi.biostd.extension.readAsPageTab
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.Companion.JSON
import ac.uk.ebi.biostd.integration.SubFormat.Companion.TSV
import ac.uk.ebi.biostd.integration.SubFormat.Companion.XML
import ac.uk.ebi.biostd.integration.SubFormat.JsonFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.Tsv
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ac.uk.ebi.biostd.integration.SubFormat.XmlFormat
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.allSections
import ebi.ac.uk.model.extensions.fileListName
import java.io.File

internal class FileListSerializer(
    private val serializer: PagetabSerializer
) {
    internal fun deserializeFileList(submission: Submission, source: FilesSource): Submission {
        submission.allSections()
            .filter { section -> section.fileListName != null }
            .map { section -> section to section.fileListName!! }
            .forEach { (section, fileList) -> section.fileList = getFileList(fileList, source) }
        return submission
    }

    private fun getFileList(fileList: String, source: FilesSource): FileList {
        val filesTable = getFilesTable(getFile(fileList, source))
        return FileList(fileList, filesTable.elements)
    }

    private fun getFile(fileList: String, source: FilesSource): File {
        return when (val bioFile = source.getFile(fileList)) {
            is FireBioFile -> TODO()
            is FireDirectoryBioFile -> TODO()
            is NfsBioFile -> bioFile.file
        }
    }

    private fun getFilesTable(file: File): FilesTable = when (SubFormat.fromFile(file)) {
        XmlFormat -> serializer.deserializeElement(file.readAsPageTab(), XML)
        is JsonFormat -> serializer.deserializeElement(file.readAsPageTab(), JSON)
        Tsv, XlsxTsv -> serializer.deserializeElement(file.readAsPageTab(), TSV)
    }
}
