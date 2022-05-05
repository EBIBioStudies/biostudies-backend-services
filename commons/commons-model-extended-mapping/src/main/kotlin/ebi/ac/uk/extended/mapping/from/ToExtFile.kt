package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.errors.FileNotFoundException
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.model.BioFile
import ebi.ac.uk.model.extensions.md5

internal const val TO_EXT_FILE_EXTENSIONS = "ebi.ac.uk.extended.mapping.from.ToExtFileKt"

// TODO: remove function as it only call source internally. Only keep to reduce impact or initial refactor.
fun BioFile.toExtFile(fileSource: FilesSource): ExtFile =
    fileSource.getExtFile(path, md5, attributes) ?: throw FileNotFoundException(path)
