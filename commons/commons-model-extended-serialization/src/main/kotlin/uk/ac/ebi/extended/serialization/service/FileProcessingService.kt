package uk.ac.ebi.extended.serialization.service

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.use
import ebi.ac.uk.util.collections.mapLeft
import uk.ac.ebi.serialization.common.FilesResolver
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Allow to process the given section, and it subsections by updating a specific attribute or modified data
 * structure.
 * Note that the submission tree is iterated from leaf sections (section with no subsections) to parents to avoid
 * update a section that has an updated child.
 *
 * @param section the section to iterate recursively.
 * @param process process function to apply to each section.
 * @return an instance of @UpdatedSection indicating if section was changed or not.
 */
fun iterateSections(section: ExtSection, process: (file: ExtSection) -> TrackSection): TrackSection {
    val sections = section.sections.map { either -> either.mapLeft { iterateSections(it, process) } }
    val (hasChanged, processedSection) = process(section)
    val changed = hasChanged || sections.any { either -> either.fold({ it.changed }, { false }) }

    return TrackSection(
        changed,
        if (changed) processedSection.copy(sections = sections.mapLeft { it.section }) else section
    )
}

class FileProcessingService(
    private val serializationService: ExtSerializationService,
    private val fileResolver: FilesResolver,
) {
    fun processFiles(
        submission: ExtSubmission,
        processFile: (file: ExtFile, index: Int) -> ExtFile,
    ): ExtSubmission {
        val index = AtomicInteger()
        val newSection = processSectionFiles(
            submission.accNo,
            submission.version,
            submission.section
        ) { processFile(it, index.incrementAndGet()) }
        return submission.copy(
            section = newSection,
            pageTabFiles = submission.pageTabFiles.map { processFile(it, index.incrementAndGet()) }
        )
    }

    private fun processSectionFiles(
        subAccNo: String,
        subVersion: Int,
        section: ExtSection,
        processFile: (file: ExtFile) -> ExtFile,
    ): ExtSection = section.copy(
        files = section.files.map { processFiles(it, processFile) },
        fileList = section.fileList?.let { processFileList(subAccNo, subVersion, it, processFile) },
        sections = section.sections.map { processSections(it, subAccNo, subVersion, processFile) }
    )

    private fun processFileList(
        subAccNo: String,
        subVersion: Int,
        fileList: ExtFileList,
        processFile: (file: ExtFile) -> ExtFile,
    ): ExtFileList {
        val newFileList = fileResolver.createExtEmptyFile(subAccNo, subVersion, fileList.fileName)
        return fileList.copy(
            file = copyFile(fileList.file, newFileList, processFile),
            pageTabFiles = fileList.pageTabFiles.map(processFile),
        )
    }

    private fun copyFile(inputFile: File, outputFile: File, processFile: (file: ExtFile) -> ExtFile): File {
        use(
            inputFile.inputStream(),
            outputFile.outputStream()
        ) { input, output ->
            serializationService.serialize(serializationService.deserializeList(input).map { processFile(it) }, output)
        }

        return outputFile
    }

    private fun processFiles(
        either: Either<ExtFile, ExtFileTable>,
        processFile: (file: ExtFile) -> ExtFile,
    ) = either.bimap(
        { extFile -> processFile(extFile) },
        { extTable -> extTable.copy(files = extTable.files.map { processFile(it) }) }
    )

    private fun processSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        subAccNo: String,
        subVersion: Int,
        processFile: (file: ExtFile) -> ExtFile,
    ) = subSection.bimap(
        { processSectionFiles(subAccNo, subVersion, it, processFile) },
        { it.copy(sections = it.sections.map { sub -> processSectionFiles(subAccNo, subVersion, sub, processFile) }) }
    )
}

data class TrackSection(val changed: Boolean, val section: ExtSection)
