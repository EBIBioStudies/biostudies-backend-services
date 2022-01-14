package ac.uk.ebi.biostd.submission.exceptions

import ebi.ac.uk.model.File

/**
 * Generated when a submission includes a reference to a file which is not present.
 */
class InvalidFilesException(fileList: String, invalidFiles: List<File>) :
    RuntimeException(buildMessage(fileList, invalidFiles)) {
    
    companion object {
        fun buildMessage(fileList: String, invalidFiles: List<File>): String = buildString {
            append("The following files were not found in file list $fileList\n")
            invalidFiles.forEach { append("- ${it.path}\n") }
        }
    }
}
