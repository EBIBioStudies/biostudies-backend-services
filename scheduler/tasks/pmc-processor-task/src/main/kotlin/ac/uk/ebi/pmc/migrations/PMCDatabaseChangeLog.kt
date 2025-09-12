package ac.uk.ebi.pmc.migrations

import ac.uk.ebi.pmc.config.ERRORS_COL
import ac.uk.ebi.pmc.config.INPUT_FILES_COL
import ac.uk.ebi.pmc.config.SUBMISSION_COL
import ac.uk.ebi.pmc.config.SUB_FILES_COL
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument.Fields.FILE_DOC_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubFileDocument.Fields.FILE_DOC_PATH
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_POS_IN_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_SOURCE_TIME
import ac.uk.ebi.pmc.persistence.docs.SubmissionDocument.Fields.SUB_STATUS
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_ACCNO
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_MODE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDocument.Fields.ERROR_UPLOADED
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index

suspend fun ReactiveMongoTemplate.executeMigrations() {
    ensureExists(ERRORS_COL)
    ensureExists(SUBMISSION_COL)
    ensureExists(SUB_FILES_COL)
    ensureExists(INPUT_FILES_COL)

    indexOps(ERRORS_COL).apply {
        createIndex(Index().on(ERROR_SOURCE_FILE, ASC)).awaitFirst()
        createIndex(Index().on(ERROR_MODE, ASC)).awaitFirst()
        createIndex(Index().on(ERROR_ACCNO, ASC)).awaitFirst()
        createIndex(Index().on(ERROR_UPLOADED, ASC)).awaitFirst()
    }
    indexOps(SUBMISSION_COL).apply {
        createIndex(Index().on(SUB_ACC_NO, ASC)).awaitFirst()
        createIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC)).awaitFirst()
        createIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC)).awaitFirst()
        createIndex(Index().on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC)).awaitFirst()
        createIndex(Index().on(SUB_STATUS, ASC)).awaitFirst()
        createIndex(Index().on(SUB_SOURCE_FILE, ASC)).awaitFirst()
        createIndex(Index().on(SUB_SOURCE_TIME, ASC)).awaitFirst()

        indexOps(INPUT_FILES_COL).apply {
            createIndex(Index().on(FILE_DOC_ACC_NO, ASC)).awaitFirst()
            createIndex(Index().on(FILE_DOC_PATH, ASC)).awaitFirst()
        }
    }
}

private suspend fun ReactiveMongoOperations.ensureExists(collection: String) {
    if (collectionExists(collection).awaitSingle().not()) createCollection(collection).awaitFirst()
}
