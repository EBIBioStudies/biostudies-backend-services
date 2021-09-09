package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.File

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

fun File.toExtFile(fileSource: FilesSource): ExtFile {
    return when (val file = fileSource.getFile(path)) {
        is FireBioFile -> FireFile(path, file.fireId, file.md5, file.size(), attributes.toExtAttributes())
        is FireDirectoryBioFile -> FireDirectory(path, file.md5, file.size, attributes.toExtAttributes())
        is NfsBioFile -> NfsFile(path, file.file, attributes.toExtAttributes())
    }
}
