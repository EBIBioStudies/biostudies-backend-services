package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.db.repositories.FileListDocFileRepository
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList

class ToExtFileListMapper(
    private val fileListDocFileRepository: FileListDocFileRepository,
) {
    /**
     * Maps a DocFileList to corresponding Ext type. Note that empty list may used if includeFileListFiles as files as
     * list files are not loaded as part of the submission.
     */
    fun toExtFileList(
        docFileList: DocFileList,
        subAccNo: String,
        subVersion: Int,
        includeFileListFiles: Boolean,
    ): ExtFileList {
        val extFiles = if (includeFileListFiles) loadFiles(subAccNo, subVersion, docFileList.fileName) else emptyList()
        return ExtFileList(
            filePath = docFileList.fileName,
            files = extFiles,
            pageTabFiles = docFileList.pageTabFiles.map { it.toExtFile() }
        )
    }

    private fun loadFiles(subAccNo: String, subVersion: Int, fileListName: String): List<ExtFile> =
        fileListDocFileRepository
            .findAllBySubmissionAccNoAndSubmissionVersionAndFileListName(subAccNo, subVersion, fileListName)
            .map { it.file.toExtFile() }
}
