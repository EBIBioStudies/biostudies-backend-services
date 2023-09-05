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
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import ebi.ac.uk.base.EMPTY
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

internal const val TITLE_INDEX_NAME = "title_index"
internal val CHANGE_LOG_CLASSES = listOf(ChangeLog001::class.java)

@ChangeLog
class ChangeLog001 {
    @ChangeSet(order = "001", id = "Create Schema Indexes", author = "System")
    fun changeSet001(template: MongockTemplate) {
        template.ensureExists(DocSubmission::class.java)
        template.ensureSubmissionIndexes<DocSubmission>()

        template.ensureExists(DocSubmissionRequest::class.java)
        template.ensureSubmissionRequestIndexes()
        template.ensureRequestFileIndexes();

        template.ensureFileListIndexes()
        template.ensureStats()
    }
}

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
inline fun <reified T> MongoOperations.ensureSubmissionIndexes(prefix: String = EMPTY) {
    indexOps(T::class.java).apply {
        ensureIndex(Index().on("$prefix$SUB_ACC_NO", ASC))
        ensureIndex(Index().on("$prefix$SUB_ACC_NO", ASC).on(SUB_VERSION, ASC))
        ensureIndex(Index().on("$prefix$SUB_OWNER", ASC))
        ensureIndex(Index().on("$prefix$SUB_SUBMITTER", ASC))
        ensureIndex(Index().on("$prefix$SUB_SECTION.$SEC_TYPE", ASC))
        ensureIndex(Index().on("$prefix$SUB_RELEASE_TIME", ASC))
        ensureIndex(Index().on("$prefix$SUB_RELEASED", ASC))
        ensureIndex(Index().on("$prefix$SUB_MODIFICATION_TIME", ASC))
        ensureIndex(Index().on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC).on(SUB_VERSION, ASC))
        ensureIndex(
            Index()
                .on("$prefix$SUB_COLLECTIONS.$COLLECTION_ACC_NO", ASC)
                .on("$prefix$SUB_VERSION", ASC)
                .on("$prefix$STORAGE_MODE", ASC)
        )
        ensureIndex(
            TextIndex()
                .onField("$prefix$SUB_TITLE")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_NAME")
                .onField("$prefix$SUB_SECTION.$SEC_ATTRIBUTES.$ATTRIBUTE_DOC_VALUE")
                .named("title_text_section.attributes.name_text_section.attributes.value_text")
                .build()
        )
    }
}

/**
 * submission_requests collection Indexes
 * 1. AccNo
 * 2. AccNo - Version
 * 3. All submission index over internal submission.
 */
fun MongoOperations.ensureSubmissionRequestIndexes() {
    ensureSubmissionIndexes<DocSubmissionRequest>("$SUB.")
    indexOps(DocSubmissionRequest::class.java).apply {
        ensureIndex(Index().on(SUB_ACC_NO, ASC))
        ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC))
    }
}

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
fun MongoOperations.ensureFileListIndexes() {
    ensureExists(FileListDocFile::class.java)
    indexOps(FileListDocFile::class.java).apply {
        // Root Index
        ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_ID, ASC))
        ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC))
        ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC))
        ensureIndex(Index().on(FILE_LIST_DOC_FILE_FILE_LIST_NAME, ASC))
        ensureIndex(Index().on(FILE_LIST_DOC_FILE_INDEX, ASC))
        ensureIndex(Index().on(FILE_DOC_FILEPATH, ASC))

        ensureIndex(
            Index()
                .on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC)
                .on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC)
                .on("$FILE_LIST_DOC_FILE_FILE.$FILE_DOC_FILEPATH", ASC)
        )
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
fun MongoOperations.ensureRequestFileIndexes() {
    ensureExists(DocSubmissionRequestFile::class.java)
    indexOps(DocSubmissionRequestFile::class.java).apply {
        ensureIndex(Index().on(RQT_FILE_SUB_ACC_NO, ASC))
        ensureIndex(Index().on(RQT_FILE_SUB_VERSION, ASC))
        ensureIndex(Index().on(RQT_FILE_PATH, ASC))
        ensureIndex(Index().on(RQT_FILE_INDEX, ASC))
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_PATH, ASC)
        )
        ensureIndex(
            Index()
                .on(RQT_FILE_SUB_ACC_NO, ASC)
                .on(RQT_FILE_SUB_VERSION, ASC)
                .on(RQT_FILE_INDEX, ASC)
        )
    }
}

/**
 * submission_stats collection indexes
 * 1. Submission AccNo
 */
fun MongoOperations.ensureStats() {
    ensureExists(DocSubmissionStats::class.java)
    indexOps(DocSubmissionRequestFile::class.java).apply {
        ensureIndex(Index().on(SUB_ACC_NO, ASC))
    }
}



