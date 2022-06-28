package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.errors.FilesProcessingException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSources
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.extensions.md5

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

fun FilesSources.toExtFile(bioFile: BioFile): ExtFile =
    getExtFile(bioFile.path, bioFile.md5, bioFile.attributes) ?: throw FilesProcessingException(bioFile.path, this)
