package ac.uk.ebi.biostd.persistence.common.exception

class FileListNotFoundException(
    name: String,
    accNo: String? = null
) : RuntimeException(exceptionMessage(name, accNo))

private fun exceptionMessage(name: String, accNo: String?): String {
    val base = "The file list '$name' could not be found"
    return accNo?.let { base.plus(" in the submission '$accNo'") } ?: base
}
