package ac.uk.ebi.biostd.submission.domain.exception

import java.io.File

class InvalidExtensionException(override val message: String) : RuntimeException(message) {
    constructor(file: File) : this("Unrecognized data format, ${file.name} can not be processed")
}
