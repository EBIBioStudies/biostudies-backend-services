package ac.uk.ebi.biostd.common

import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_FILE_PATH
import uk.ac.ebi.biostd.base.isNotBlank

fun validatedFilePath(fileName: String?): String {
    require(fileName.isNotBlank()) { throw InvalidElementException(REQUIRED_FILE_PATH) }
    return fileName!!
}
