package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.File

class InvalidFilesException(val invalidFiles: List<File>, message: String) : RuntimeException(message)