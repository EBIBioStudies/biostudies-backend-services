package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles

/**
 * Return a sequence with all the files of a submission. Pagetab files are retrieved first, followed by section files
 * and file list files.
 */
fun ExtSerializationService.fileSequence(submission: ExtSubmission): Sequence<ExtFile> {
    return sequence {
        submission.pageTabFiles.forEach { yield(it) }
        submission.allFileList.forEach { fileList -> fileList.pageTabFiles.forEach { yield(it) } }

        submission.allSectionsFiles.forEach { yield(it) }
        submission.allFileList
            .map { it.file }
            .forEach { it.inputStream().use { stream -> deserializeList(stream).forEach { file -> yield(file) } } }
    }
}
