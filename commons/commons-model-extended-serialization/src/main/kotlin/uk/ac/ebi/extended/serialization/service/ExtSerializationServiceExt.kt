package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles

fun ExtSerializationService.fileSequence(submission: ExtSubmission): Sequence<ExtFile> {
    return sequence {
        for (fileList in submission.allFileList) {
            fileList.file.inputStream().use { deserializeList(it).forEach { yield(it) } }
            fileList.pageTabFiles.forEach { yield(it) }
        }
        submission.allSectionsFiles.forEach { yield(it) }
        submission.pageTabFiles.forEach { yield(it) }
    }
}
