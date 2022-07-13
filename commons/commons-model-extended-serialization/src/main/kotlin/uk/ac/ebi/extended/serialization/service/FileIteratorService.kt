package uk.ac.ebi.extended.serialization.service

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import java.util.concurrent.atomic.AtomicInteger

class FileIteratorService(
    private val serializationService: ExtSerializationService,
) {
    fun forEachFile(
        submission: ExtSubmission,
        processFile: (file: ExtFile, index: Int) -> Unit,
    ) {
        val index = AtomicInteger()
        processSectionFiles(
            submission.accNo,
            submission.version,
            submission.section
        ) { processFile(it, index.incrementAndGet()) }
    }

    private fun processSectionFiles(
        subAccNo: String,
        subVersion: Int,
        section: ExtSection,
        processFile: (file: ExtFile) -> Unit,
    ) {
        section.files.forEach { processFiles(it, processFile) }
        section.fileList?.let { processFileList(it, processFile) }
        section.sections.forEach { processSections(it, subAccNo, subVersion, processFile) }
    }

    private fun processFileList(fileList: ExtFileList, processFile: (file: ExtFile) -> Unit) =
        fileList.file.inputStream().use { stream ->
            serializationService.deserializeList(stream).forEach { processFile(it) }
        }

    private fun processFiles(
        either: Either<ExtFile, ExtFileTable>,
        processFile: (file: ExtFile) -> Unit,
    ) {
        either.fold(
            { extFile -> processFile(extFile) },
            { extTable -> extTable.files.forEach { processFile(it) } }
        )
    }

    private fun processSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        subAccNo: String,
        subVersion: Int,
        processFile: (file: ExtFile) -> Unit,
    ) {
        subSection.fold(
            { processSectionFiles(subAccNo, subVersion, it, processFile) },
            { extTable -> extTable.sections.map { sub -> processSectionFiles(subAccNo, subVersion, sub, processFile) } }
        )
    }
}
