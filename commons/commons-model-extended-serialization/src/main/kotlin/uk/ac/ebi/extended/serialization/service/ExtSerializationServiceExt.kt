package uk.ac.ebi.extended.serialization.service

import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.allFileList
import ebi.ac.uk.extended.model.allPageTabFiles
import ebi.ac.uk.extended.model.allSectionsFiles
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Returns a flow with all the files of a submission. Pagetab files are retrieved first, followed by section files
 * and file list files.
 */
fun ExtSerializationService.filesFlow(submission: ExtSubmission): Flow<ExtFile> = filesFlowExt(submission).map { it.second }

/**
 * Returns a pair of boolean to files of a submission. The boolean flag indicates whether the file is PageTab. Files are
 * retrieved first, followed by section files and file list files.
 */
fun ExtSerializationService.filesFlowExt(submission: ExtSubmission): Flow<Pair<Boolean, ExtFile>> =
    flow {
        submission.allPageTabFiles.forEach { emit(true to it) }
        submission.allSectionsFiles.forEach { emit(false to it) }
        submission.allFileList
            .map { it.file }
            .forEach { it.inputStream().use { stream -> emitAll(deserializeListAsFlow(stream).map { false to it }) } }
    }
