package ac.uk.ebi.biostd.persistence.filesystem.service

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.io.use
import uk.ac.ebi.extended.serialization.service.ExtFilesResolver
import uk.ac.ebi.extended.serialization.service.ExtSerializationService
import java.io.File

class FileProcessingService(
    private val serializationService: ExtSerializationService,
    private val fileResolver: ExtFilesResolver,
) {
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
    fun process(section: ExtSection, process: (file: ExtSection) -> Section): Section {
        val sections = section.sections.map { either -> either.mapLeft { process(it, process) } }
        val current = process(section)
        val changed = current.changed || sections.any { either -> either.fold({ it.changed }, { false }) }

        return Section(
            changed,
            if (changed) current.section.copy(sections = sections.map { it.mapLeft(Section::section) }) else section
        )
    }

    fun processFiles(
        submission: ExtSubmission,
        processFile: (file: ExtFile) -> ExtFile,
    ): ExtSubmission = submission.copy(
        section = processSectionFiles(
            submission.accNo,
            submission.version,
            submission.section,
            processFile
        )
    )

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
        val newFileList = fileResolver.createEmptyFile(subAccNo, subVersion, fileList.fileName)
        return fileList.copy(file = copyFile(fileList.file, newFileList, processFile))
    }

    private fun copyFile(inputFile: File, outputFile: File, processFile: (file: ExtFile) -> ExtFile): File {
        use(
            inputFile.inputStream(),
            outputFile.outputStream()
        ) { input, output ->
            serializationService.serialize(serializationService.deserialize(input).map { processFile(it) }, output)
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
