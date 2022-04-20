package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles

fun ExtSerializationService.allSubmissionFiles(submission: ExtSubmission) =
    allFileListFiles(submission).plus(submission.allSectionsFiles).plus(submission.pageTabFiles)

fun ExtSerializationService.allFileListFiles(submission: ExtSubmission): Sequence<ExtFile> =
    submission.allFileList.asSequence().flatMap { fileListFiles(it) }

private fun ExtSerializationService.fileListFiles(fileList: ExtFileList): Sequence<ExtFile> =
    fileList.file!!.inputStream().use { deserialize(it) }.plus(fileList.pageTabFiles)
