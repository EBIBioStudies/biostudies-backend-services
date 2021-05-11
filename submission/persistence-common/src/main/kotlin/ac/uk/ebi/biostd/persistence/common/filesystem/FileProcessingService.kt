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
    val tempFolder: File,
    val filePermissions: Set<PosixFilePermission>,
    val dirPermissions: Set<PosixFilePermission>
)

internal fun FileProcessingConfig.copy(extFile: ExtFile): ExtFile {
    val source = extFile.file
    val target = subFolder.resolve(extFile.fileName)
    val current = tempFolder.resolve(extFile.fileName)

    logger.info { "copying file ${source.absolutePath} into ${target.absolutePath}" }

    when {
        current.exists() && source.md5() == current.md5() -> moveFile(current, target, filePermissions, dirPermissions)
        else -> copyOrReplaceFile(source, target, filePermissions, dirPermissions)
    }

    return extFile.copy(file = target)
}

internal fun FileProcessingConfig.move(extFile: ExtFile): ExtFile {
    val source = if (extFile.file.startsWith(subFolder)) tempFolder.resolve(extFile.fileName) else extFile.file
    val target = subFolder.resolve(extFile.fileName)

    if (target.notExist()) {
        logger.info { "moving file ${source.absolutePath} into ${target.absolutePath}" }
        moveFile(source, target, filePermissions, dirPermissions)
    }

    return extFile.copy(file = target)
}
