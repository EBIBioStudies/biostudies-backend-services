package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allSectionsFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

/**
 * Return a flow with all the files of a submission. Pagetab files are retrieved first, followed by section files
 * and file list files.
 */
fun ExtSerializationService.filesFlow(submission: ExtSubmission): Flow<ExtFile> {
    return flow {
        submission.pageTabFiles.forEach { emit(it) }
        submission.allFileList.forEach { fileList -> fileList.pageTabFiles.forEach { emit(it) } }
        submission.allSectionsFiles.forEach { emit(it) }
        submission.allFileList
            .map { it.file }
            .forEach { it.inputStream().use { stream -> emitAll(deserializeListAsFlow(stream)) } }
    }
}
