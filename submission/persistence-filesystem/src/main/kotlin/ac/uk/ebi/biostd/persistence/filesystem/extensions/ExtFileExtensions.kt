package ac.uk.ebi.biostd.persistence.filesystem.extensions

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType
import uk.ac.ebi.fire.client.model.FileType

val ExtFile.fireType: FileType
    get() =
        when (type) {
            ExtFileType.FILE -> FileType.FILE
            ExtFileType.DIR -> FileType.DIR
        }
