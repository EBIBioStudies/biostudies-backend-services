package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles

fun ExtSerializationService.forEachSubmissionFile(
    submission: ExtSubmission,
    function: (ExtFile) -> Unit
) {
    for (fileList in submission.allFileList) {
        fileList.file.inputStream().use { deserializeList(it).forEach(function) }
        fileList.pageTabFiles.forEach(function)
    }
    submission.allSectionsFiles.forEach(function)
    submission.pageTabFiles.forEach(function)
}
