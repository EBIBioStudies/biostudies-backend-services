package ebi.ac.uk.extended.mapping.from

import ebi.ac.uk.io.sources.FileSourcesList
import ebi.ac.uk.model.BioFile
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.RequestFile

suspend fun FileSourcesList.getExtFile(
    file: BioFile,
    fileListName: String? = null,
): ExtFile = getExtFile(file.path, file.type, file.attributes.toExtAttributes(), fileListName)

suspend fun FileSourcesList.findExtFile(file: BioFile): ExtFile? = findExtFile(file.path, file.type, file.attributes.toExtAttributes())

suspend fun FileSourcesList.getExtFile(file: ExtFile): ExtFile =
    when (file) {
        is RequestFile -> getExtFile(file.filePath, file.type, file.attributes)
        else -> file
    }
