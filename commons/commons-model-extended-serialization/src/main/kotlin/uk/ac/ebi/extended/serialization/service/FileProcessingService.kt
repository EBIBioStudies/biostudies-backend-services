package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.base.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.use
import ebi.ac.uk.util.collections.mapLeft
import kotlinx.coroutines.flow.map
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
fun iterateSections(
    section: ExtSection,
    process: (file: ExtSection) -> TrackSection,
): TrackSection {
    val sections = section.sections.map { either -> either.mapLeft { iterateSections(it, process) } }
    val (hasChanged, processedSection) = process(section)
    val changed = hasChanged || sections.any { either -> either.fold({ it.changed }, { false }) }

    return TrackSection(
        changed,
        if (changed) processedSection.copy(sections = sections.mapLeft { it.section }) else section,
    )
}

typealias ProcessFunction = suspend (file: ExtFile) -> ExtFile
typealias ProcessFunctionIndexed = (file: ExtFile, index: Int) -> ExtFile

class FileProcessingService(
    private val serializationService: ExtSerializationService,
    private val fileResolver: FilesResolver,
) {
    suspend fun processFilesIndexed(
        submission: ExtSubmission,
        processFile: ProcessFunctionIndexed,
    ): ExtSubmission {
        val index = AtomicInteger()
        return processFiles(submission) { file -> processFile(file, index.incrementAndGet()) }
    }

    suspend fun processFiles(
        submission: ExtSubmission,
        processFile: ProcessFunction,
    ): ExtSubmission {
        val newSection =
            processSectionFiles(
                submission.accNo,
                submission.version,
                submission.section,
            ) { processFile(it) }
        return submission.copy(
            section = newSection,
            pageTabFiles = submission.pageTabFiles.map { processFile(it) },
        )
    }

    private suspend fun processSectionFiles(
        subAccNo: String,
        subVersion: Int,
        section: ExtSection,
        processFile: ProcessFunction,
    ): ExtSection =
        section.copy(
            files = section.files.map { processFiles(it, processFile) },
            fileList = section.fileList?.let { processFileList(subAccNo, subVersion, it, processFile) },
            sections = section.sections.map { processSections(it, subAccNo, subVersion, processFile) },
        )

    private suspend fun processFileList(
        subAccNo: String,
        subVersion: Int,
        fileList: ExtFileList,
        processFile: ProcessFunction,
    ): ExtFileList {
        val newFileList = fileResolver.createRequestTempFile(subAccNo, subVersion, fileList.fileName)
        return fileList.copy(
            file = copyFile(fileList.file, newFileList, processFile),
            pageTabFiles = fileList.pageTabFiles.map { processFile(it) },
        )
    }

    private suspend fun copyFile(
        inputFile: File,
        outputFile: File,
        processFile: ProcessFunction,
    ): File {
        use(
            inputFile.inputStream(),
            outputFile.outputStream(),
        ) { input, output ->
            val files = serializationService.deserializeListAsFlow(input)
            serializationService.serialize(files.map { processFile(it) }, output)
        }

        return outputFile
    }

    private suspend fun processFiles(
        either: Either<ExtFile, ExtFileTable>,
        processFile: ProcessFunction,
    ) = either.bimap(
        { extFile -> processFile(extFile) },
        { extTable -> extTable.copy(files = extTable.files.map { processFile(it) }) },
    )

    private suspend fun processSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        subAccNo: String,
        subVersion: Int,
        processFile: ProcessFunction,
    ) = subSection.bimap(
        { processSectionFiles(subAccNo, subVersion, it, processFile) },
        { it.copy(sections = it.sections.map { sub -> processSectionFiles(subAccNo, subVersion, sub, processFile) }) },
    )
}

data class TrackSection(val changed: Boolean, val section: ExtSection)
