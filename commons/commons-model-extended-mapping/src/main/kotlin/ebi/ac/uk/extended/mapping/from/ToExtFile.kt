package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FILES_RESERVED_ATTRS
import ebi.ac.uk.model.extensions.md5

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

fun File.toExtFile(fileSource: FilesSource, calculateProperties: Boolean = true): ExtFile {
    return when (val file = fileSource.getFile(path, md5)) {
        is FireBioFile -> FireFile(
            path,
            "Files/$path",
            file.fireId,
            file.md5,
            file.size(),
            attributes.toExtAttributes(FILES_RESERVED_ATTRS)
        )
        is FireDirectoryBioFile -> FireDirectory(
            path,
            "Files/$path",
            file.fireId,
            file.md5,
            file.size,
            attributes.toExtAttributes(FILES_RESERVED_ATTRS)
        )
        is NfsBioFile -> NfsFile(
            path,
            "Files/$path",
            file.file,
            file.file.absolutePath,
            if (calculateProperties) file.md5() else "NOT_CALCULATED",
            if (calculateProperties) file.size() else -1,
            attributes.toExtAttributes(FILES_RESERVED_ATTRS)
        )
        null -> throw FileNotFoundException(path)
    }
}
