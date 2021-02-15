package ac.uk.ebi.biostd.persistence.common.filesystem

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.FileMode
import ebi.ac.uk.extended.model.FileMode.COPY
import ebi.ac.uk.io.FileUtils.copyOrReplaceFile
import ebi.ac.uk.io.FileUtils.moveFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.notExist
import mu.KotlinLogging
import java.io.File
import java.nio.file.attribute.PosixFilePermission

private val logger = KotlinLogging.logger {}

class FileProcessingService {
    fun processFiles(processRequest: FileProcessingRequest): ExtSubmission {
        val (mode, submission, config) = processRequest
        val processFunc: (file: ExtFile) -> ExtFile = if (mode == COPY) config::copy else config::move

        return submission.copy(section = processSection(submission.section, processFunc))
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
        { extTable -> extTable.copy(files = extTable.files.map { processFile(it) }) })

    private fun processSections(
        subSection: Either<ExtSection, ExtSectionTable>,
        processFile: (file: ExtFile) -> ExtFile
    ) = subSection.bimap(
        { processSection(it, processFile) },
        { it.copy(sections = it.sections.map { subSect -> processSection(subSect, processFile) }) })
}

data class FileProcessingRequest(
    val mode: FileMode,
    val submission: ExtSubmission,
    val config: FileProcessingConfig
)

data class FileProcessingConfig(
    val subFolder: File,
    val targetFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val folderPermissions: Set<PosixFilePermission>
)

private fun FileProcessingConfig.copy(extFile: ExtFile): ExtFile {
    val source = extFile.file
    val target = targetFolder.resolve(extFile.fileName)
    val current = subFolder.resolve(extFile.fileName)

    if (current.notExist() || source.md5() != current.md5()) {
        logger.info { "copying file ${source.absolutePath} into ${target.absolutePath}" }
        copyOrReplaceFile(source, target, filePermissions, folderPermissions)

        return extFile.copy(file = target)
    }

    moveFile(current, target, filePermissions, folderPermissions)
    return extFile.copy(file = target)
}

private fun FileProcessingConfig.move(extFile: ExtFile): ExtFile {
    val source = extFile.file
    val target = targetFolder.resolve(extFile.fileName)
    val current = subFolder.resolve(extFile.fileName)

    if (target.notExist() && (current.notExist() || source.md5() != current.md5())) {
        logger.info { "moving file ${source.absolutePath} into ${target.absolutePath}" }
        moveFile(source, target, filePermissions, folderPermissions)
    }

    return extFile.copy(file = target)
}
