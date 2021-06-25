package ac.uk.ebi.biostd.persistence.filesystem.service

import ac.uk.ebi.biostd.persistence.filesystem.fire.fireCopy
import ac.uk.ebi.biostd.persistence.filesystem.fire.fireMove
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsCopy
import ac.uk.ebi.biostd.persistence.filesystem.nfs.nfsMove
import ac.uk.ebi.biostd.persistence.filesystem.request.FileProcessingRequest
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile

class FileProcessingService {
    fun processFiles(processRequest: FileProcessingRequest): ExtSubmission {
        val (mode, submission, config) = processRequest

        fun processFile(file: ExtFile): ExtFile = when (file) {
            is FireFile -> if (mode == COPY) config.fireCopy(file) else config.fireMove(file)
            is NfsFile -> if (mode == COPY) config.nfsCopy(file) else config.nfsMove(file)
        }

        return submission.copy(section = processSection(submission.section, ::processFile))
    }

    private fun processSection(
        section: ExtSection,
        processFile: (file: ExtFile) -> ExtFile
    ): ExtSection = section.copy(
        files = section.files.map { processFiles(it, processFile) },
        fileList = section.fileList?.let { processFileList(it, processFile) },
        sections = section.sections.map { processSections(it, processFile) }
    )

    private fun processFileList(
        fileList: ExtFileList,
        processFile: (file: ExtFile) -> ExtFile
    ) = fileList.copy(files = fileList.files.map { processFile(it) })

    private fun processFiles(
        either: Either<ExtFile, ExtFileTable>,
        processFile: (file: ExtFile) -> ExtFile
    ) = either.bimap(
        { extFile -> processFile(extFile) },
        { extTable -> extTable.copy(files = extTable.files.map { processFile(it) }) }
    )

    private fun processSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        processFile: (file: ExtFile) -> ExtFile
    ) = subSection.bimap(
        { processSection(it, processFile) },
        { it.copy(sections = it.sections.map { subSect -> processSection(subSect, processFile) }) }
    )
}
