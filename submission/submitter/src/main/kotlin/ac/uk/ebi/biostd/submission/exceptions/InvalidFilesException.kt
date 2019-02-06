package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.File

/**
 * Generated when a submission includes a reference to a file which is not present.
 */
class InvalidFilesException(val invalidFiles: List<File>, message: String) : RuntimeException(message)
