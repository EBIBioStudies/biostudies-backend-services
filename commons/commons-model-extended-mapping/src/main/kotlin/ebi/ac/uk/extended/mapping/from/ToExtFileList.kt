package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FileList

class ToExtFileList {
    fun convert(fileList: FileList, fileSource: FilesSource): ExtFileList =
        ExtFileList(fileList.name.substringBeforeLast("."), toExtFiles(fileSource, fileList.referencedFiles))
}

private fun toExtFiles(fileSource: FilesSource, files: List<File>): List<ExtFile> {
    return files
        .asSequence()
        .map { it.toExtFile(fileSource, false) }
        .toList()
}
