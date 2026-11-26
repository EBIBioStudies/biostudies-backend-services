package uk.ac.ebi.biostd.common

import uk.ac.ebi.biostd.base.isNotBlank
import uk.ac.ebi.biostd.validation.InvalidElementException
import uk.ac.ebi.biostd.validation.REQUIRED_FILE_PATH

fun validatedFilePath(fileName: String?): String {
    require(fileName.isNotBlank()) { throw InvalidElementException(REQUIRED_FILE_PATH) }
    return fileName!!
}
