package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

fun FileSourcesList.toExtFile(bioFile: BioFile): ExtFile {
    val file = getExtFile(bioFile.path, bioFile.attributes)
    return file ?: throw FilesProcessingException(bioFile.path, this)
}
