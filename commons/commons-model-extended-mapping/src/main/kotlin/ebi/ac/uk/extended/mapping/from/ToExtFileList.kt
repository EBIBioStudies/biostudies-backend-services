package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
internal const val TO_EXT_LIBRARY_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileListKt"

fun FileList.toExtFileList(fileSource: FilesSource): ExtFileList =
    ExtFileList(name.substringBeforeLast("."), toExtFiles(fileSource, referencedFiles))

private fun toExtFiles(fileSource: FilesSource, files: List<File>): List<ExtFile> {
    val filesCount = files.count()
    return files
        .asSequence()
        .onEachIndexed { index, file -> logger.info { "mapping file ${file.path}, ${index + 1} of $filesCount" } }
        .map { it.toExtFile(fileSource) }
        .toList()
}

