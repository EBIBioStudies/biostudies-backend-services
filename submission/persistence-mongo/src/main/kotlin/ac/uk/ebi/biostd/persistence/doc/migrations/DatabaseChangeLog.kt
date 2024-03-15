package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.commons.ensureExists
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.STORAGE_MODE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_COLLECTIONS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_PATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_COLLECTION_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FireDocFileFields.FIRE_FILE_DOC_PUBLISHED
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.extended.model.StorageMode
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation.lookup
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

suspend fun ReactiveMongoTemplate.executeMigrations() {
    ensureExists(DocSubmission::class.java)
    ensureSubmissionIndexes()

    ensureExists(DocSubmissionRequest::class.java)
    ensureSubmissionRequestIndexes()
    ensureRequestFileIndexes()

    ensureFileListIndexes()
    ensureStatsIndexes()

    publishedFlag()
}

suspend fun ReactiveMongoOperations.ensureSubmissionIndexes() = ensureSubmissionIndexes<DocSubmission>()

/**
 * Submission Indexes
 * 1. AccNo
 * 2. AccNo - Version
 * 3. Owner
 * 4. Submitter
 * 5. Root Section Type
 * 6. Release Time
 * 7. Released
 * 8. Modification Time
 * 9. Collection AccNo , Submission version
 * 10. Collection AccNo , Submission Version, Submission Storage Mode
 * 11. (Text Index) Submission Title, Submission Attributes, Section Attributes
 */
private suspend inline fun <reified T> ReactiveMongoOperations.ensureSubmissionIndexes(prefix: String = EMPTY) {
    indexOps(T::class.java).apply {
        ensureIndex(backgroundIndex().on("$prefix$SUB_ACC_NO", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_ACC_NO", ASC).on(SUB_VERSION, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_OWNER", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_SUBMITTER", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_SECTION.$SEC_TYPE", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_RELEASE_TIME", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_RELEASED", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_MODIFICATION_TIME", ASC)).awaitSingleOrNull()
        ensureIndex(
            backgroundIndex().on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC).on(SUB_VERSION, ASC)
        ).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC)
                .on("$prefix$SUB_VERSION", ASC)
                .on("$prefix$STORAGE_MODE", ASC)
        ).awaitSingleOrNull()
        ensureIndex(
            TextIndex()
                .onField("$prefix$SUB_TITLE")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_NAME")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_VALUE")
                .named("title_text_section.attributes.name_text_section.attributes.value_text")
                .build()
        ).awaitSingleOrNull()
    }
}

/**
 * submission_requests collection Indexes
 * 1. AccNo
 * 2. AccNo - Version
 * 3. All submission index over internal submission.
 */
suspend fun ReactiveMongoOperations.ensureSubmissionRequestIndexes() {
    ensureSubmissionIndexes<DocSubmissionRequest>("$SUB.")
    indexOps(DocSubmissionRequest::class.java).apply {
        ensureIndex(backgroundIndex().on(SUB_ACC_NO, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC)).awaitSingleOrNull()
    }
}

fun backgroundIndex() = Index().background()

/**
 * file_list_files collection indexes
 * 1. Submission ID
 * 2. Submission AccNo
 * 3. Submission Version
 * 4. File List File Name
 * 5. Index
 * 6. Path
 * 7. Submission AccNo, Submission Version, File.Path
 */
suspend fun ReactiveMongoOperations.ensureFileListIndexes() {
    ensureExists(FileListDocFile::class.java)
    indexOps(FileListDocFile::class.java).apply {
        ensureIndex(backgroundIndex().on(FILE_LIST_DOC_FILE_SUBMISSION_ID, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(FILE_LIST_DOC_FILE_FILE_LIST_NAME, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(FILE_LIST_DOC_FILE_INDEX, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(FILE_DOC_FILEPATH, ASC)).awaitSingleOrNull()

        ensureIndex(
            Index()
                .on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC)
                .on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC)
                .on("$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_FILEPATH", ASC)
        ).awaitSingleOrNull()
    }
}

/**
 * submission_request_files collection indexes
 * 1. Submission AccNo
 * 2. Submission Version
 * 4. Path
 * 5. Index
 * 6. Submission AccNo, Submission Version, File.Path
 */
suspend fun ReactiveMongoOperations.ensureRequestFileIndexes() {
    ensureExists(DocSubmissionRequestFile::class.java)
    indexOps(DocSubmissionRequestFile::class.java).apply {
        ensureIndex(backgroundIndex().on(RQT_FILE_SUB_ACC_NO, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(RQT_FILE_SUB_VERSION, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(RQT_FILE_PATH, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(RQT_FILE_INDEX, ASC)).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_PATH, ASC)
        ).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_INDEX, ASC)
        ).awaitSingleOrNull()
    }
}

/**
 * submission_stats collection indexes
 * 1. Submission AccNo
 */
suspend fun ReactiveMongoOperations.ensureStatsIndexes() {
    ensureExists(DocSubmissionStats::class.java)
    indexOps(DocSubmissionStats::class.java).apply {
        ensureIndex(backgroundIndex().on(SUB_ACC_NO, ASC)).awaitSingleOrNull()
    }
}

/**
 * Published flag for all the submission files
 */
suspend fun ReactiveMongoOperations.publishedFlag() {
    suspend fun setReleasedFlag(submission: Submission) {
        val query = Query(
            where(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO).`is`(submission.accNo)
                .andOperator(where(FILE_LIST_DOC_FILE_SUBMISSION_VERSION).`is`(submission.version))
        )

        updateMulti(
            query,
            Update().set("$FILE_LIST_DOC_FILE_FILE.$FIRE_FILE_DOC_PUBLISHED", submission.released),
            FileListDocFile::class.java
        ).awaitSingleOrNull()
    }

    val aggregation = newAggregation(
        DocSubmission::class.java,
        match(where(SUB_VERSION).gt(0).andOperator(where(STORAGE_MODE).`is`(StorageMode.FIRE.value))),
        lookup(FILE_LIST_DOC_FILE_COLLECTION_NAME, SUB_ACC_NO, FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, "result"),
        match(where("result").not().size(0)),
        project(SUB_ACC_NO, SUB_VERSION, SUB_RELEASED)
    )

    aggregate(aggregation, Submission::class.java)
        .asFlow()
        .map { setReleasedFlag(it) }
        .collect()
}

data class Submission(
    val accNo: String,
    val version: Int,
    val released: Boolean,
)
