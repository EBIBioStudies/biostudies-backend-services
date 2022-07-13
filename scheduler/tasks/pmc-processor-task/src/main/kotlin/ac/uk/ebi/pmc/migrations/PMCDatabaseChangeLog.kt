package ac.uk.ebi.pmc.migrations

import ac.uk.ebi.pmc.config.ERRORS_COL
import ac.uk.ebi.pmc.config.INPUT_FILES_COL
import ac.uk.ebi.pmc.config.SUBMISSION_COL
import ac.uk.ebi.pmc.config.SUB_FILES_COL
import ac.uk.ebi.pmc.persistence.docs.FileDoc.Fields.FILE_DOC_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.FileDoc.Fields.FILE_DOC_PATH
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_ACC_NO
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_POS_IN_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_SOURCE_TIME
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc.Fields.SUB_STATUS
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_ACCNO
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_MODE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_SOURCE_FILE
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc.Fields.ERROR_UPLOADED
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.index.Index

@ChangeLog
class PMCDatabaseChangeLog {

    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun createSchema(template: MongockTemplate) {
        template.createCollectionByNameIfNotExists(ERRORS_COL)
        template.createCollectionByNameIfNotExists(SUBMISSION_COL)
        template.createCollectionByNameIfNotExists(SUB_FILES_COL)
        template.createCollectionByNameIfNotExists(INPUT_FILES_COL)

        template.indexOps(ERRORS_COL).apply {
            ensureIndex(Index().on(ERROR_SOURCE_FILE, ASC))
            ensureIndex(Index().on(ERROR_MODE, ASC))
            ensureIndex(Index().on(ERROR_ACCNO, ASC))
            ensureIndex(Index().on(ERROR_UPLOADED, ASC))
        }
        template.indexOps(SUBMISSION_COL).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC))
            ensureIndex(Index().on(SUB_SOURCE_TIME, ASC).on(SUB_POS_IN_FILE, ASC))
            ensureIndex(Index().on(SUB_STATUS, ASC))
            ensureIndex(Index().on(SUB_SOURCE_FILE, ASC))
            ensureIndex(Index().on(SUB_SOURCE_TIME, ASC))
        }
        template.indexOps(INPUT_FILES_COL).apply {
            ensureIndex(Index().on(FILE_DOC_ACC_NO, ASC))
            ensureIndex(Index().on(FILE_DOC_PATH, ASC))
        }
    }
}
