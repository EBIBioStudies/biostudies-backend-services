package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.commons.ensureExists
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocAttributeFields.ATTRIBUTE_DOC_VALUE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields.FILE_DOC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_PROCESS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocRequestFields.RQT_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_DIRECTORIES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_FILE_SIZE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocStatsFields.STATS_NON_DECLARED_FILES_DIRECTORIES
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
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_STATUS
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionRequestFileFields.RQT_FILE_SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_FILE_LIST_NAME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_INDEX
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_ID
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.FileListDocFileFields.FILE_LIST_DOC_FILE_SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ebi.ac.uk.base.EMPTY
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.query.Collation
import org.springframework.data.mongodb.core.query.Collation.ComparisonLevel
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

suspend fun ReactiveMongoTemplate.executeMigrations() {
    ensureExists(DocSubmission::class.java)
    ensureSubmissionIndexes()

    ensureExists(DocSubmissionRequest::class.java)
    ensureSubmissionRequestIndexes()
    ensureRequestFileIndexes()

    ensureFileListIndexes()
    ensureStatsIndexes()
}

suspend fun ReactiveMongoOperations.ensureSubmissionIndexes() = ensureSubmissionIndexes<DocSubmission>()

/**
 * Submission Indexes
 * 1. AccNo
 * 2. AccNo - Version
 * 3. Owner (Case insensitive)
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
        ensureIndex(
            backgroundIndex()
                .on("$prefix$SUB_OWNER", ASC)
                .collation(Collation.of("en").strength(ComparisonLevel.secondary())),
        ).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_SUBMITTER", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_SECTION.$SEC_TYPE", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_RELEASE_TIME", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_RELEASED", ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on("$prefix$SUB_MODIFICATION_TIME", ASC)).awaitSingleOrNull()
        ensureIndex(
            backgroundIndex().on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC).on(SUB_VERSION, ASC),
        ).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC)
                .on("$prefix$SUB_VERSION", ASC)
                .on("$prefix$STORAGE_MODE", ASC),
        ).awaitSingleOrNull()
        ensureIndex(
            TextIndex()
                .onField("$prefix$SUB_TITLE")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_NAME")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_VALUE")
                .named("title_text_section.attributes.name_text_section.attributes.value_text")
                .build(),
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
    ensureSubmissionIndexes<DocSubmissionRequest>("$RQT_PROCESS.$SUB.")
    indexOps(DocSubmissionRequest::class.java).apply {
        ensureIndex(backgroundIndex().on(SUB_ACC_NO, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(RQT_STATUS, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(RQT_STATUS, ASC).on(RQT_MODIFICATION_TIME, ASC)).awaitSingleOrNull()
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
                .on("$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_FILEPATH", ASC),
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
 * 7. Submission AccNo, Submission Version, File Status
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
                .on(RQT_FILE_PATH, ASC),
        ).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_INDEX, ASC),
        ).awaitSingleOrNull()
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_STATUS, ASC),
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
        ensureIndex(backgroundIndex().on(STATS_ACC_NO, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(STATS_FILE_SIZE, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(STATS_DIRECTORIES, ASC)).awaitSingleOrNull()
        ensureIndex(backgroundIndex().on(STATS_NON_DECLARED_FILES_DIRECTORIES, ASC)).awaitSingleOrNull()
    }
}
