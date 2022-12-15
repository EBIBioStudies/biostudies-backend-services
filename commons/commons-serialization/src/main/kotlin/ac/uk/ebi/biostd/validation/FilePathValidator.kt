package ac.uk.ebi.biostd.validation

import ebi.ac.uk.base.isNotBlank

private val pathRestrictions = "[\\.*][\\/]".toRegex()

fun validateFilePath(path: String?) {
    require(path.isNotBlank()) { throw InvalidElementException(REQUIRED_FILE_PATH) }
    require(pathRestrictions.containsMatchIn(path!!).not()) { throw InvalidElementException(INVALID_FILE_PATH) }
}
