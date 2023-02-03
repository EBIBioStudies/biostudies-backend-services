package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.commons.ensureExists
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocFileFields
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_STATS
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
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.Companion.STATUS
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft.DraftStatus.ACTIVE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequestFile
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionStats
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import ebi.ac.uk.model.constants.SectionFields.TITLE
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.PartialIndexFilter
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

internal const val TITLE_INDEX_NAME = "title_index"
internal val CHANGE_LOG_CLASSES = listOf(
    ChangeLog001::class.java,
    ChangeLog002::class.java,
    ChangeLog003::class.java,
    ChangeLog004::class.java,
    ChangeLog005::class.java,
    ChangeLog006::class.java,
)

@ChangeLog
class ChangeLog001 {
    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun changeSet001(template: MongockTemplate) {
        template.ensureExists(DocSubmission::class.java)
        template.ensureExists(DocSubmissionRequest::class.java)

        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC))
            ensureIndex(Index().on(SUB_OWNER, ASC))
            ensureIndex(Index().on(SUB_SUBMITTER, ASC))
            ensureIndex(Index().on("$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on(SUB_RELEASE_TIME, ASC))
            ensureIndex(TextIndex().named(TITLE_INDEX_NAME).onField(SUB_TITLE).build())
            ensureIndex(Index().on(SUB_RELEASED, ASC))
        }

        template.indexOps(DocSubmissionRequest::class.java).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC))
            ensureIndex(Index().on("$SUB.$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on("$SUB.$SUB_ACC_NO", ASC))
            ensureIndex(Index().on("$SUB.$SUB_OWNER", ASC))
            ensureIndex(Index().on("$SUB.$SUB_SUBMITTER", ASC))
            ensureIndex(Index().on("$SUB.$SUB_RELEASE_TIME", ASC))
            ensureIndex(TextIndex().named(TITLE_INDEX_NAME).onField("$SUB.$SUB_TITLE").build())
            ensureIndex(Index().on("$SUB.$SUB_RELEASED", ASC))
        }
    }
}

@ChangeLog
class ChangeLog002 {
    @ChangeSet(order = "002", id = "Section Title Index", author = "System")
    fun changeSet002(template: MongockTemplate) {
        template.ensureExists(DocSubmission::class.java)
        template.ensureExists(DocSubmissionRequest::class.java)

        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(TextIndex().named(TITLE_INDEX_NAME).onField(SUB_TITLE).build())
            dropIndex(TITLE_INDEX_NAME)
            ensureIndex(
                TextIndex()
                    .named(TITLE_INDEX_NAME)
                    .onField(SUB_TITLE)
                    .partial(PartialIndexFilter.of(where("$SUB_SECTION.$SUB_ATTRIBUTES.name").`is`(TITLE.value)))
                    .onField("$SUB_SECTION.$SUB_ATTRIBUTES.value")
                    .build()
            )
        }

        template.indexOps(DocSubmissionRequest::class.java).apply {
            ensureIndex(TextIndex().named(TITLE_INDEX_NAME).onField("$SUB.$SUB_TITLE").build())
            dropIndex(TITLE_INDEX_NAME)
            ensureIndex(
                TextIndex()
                    .named(TITLE_INDEX_NAME)
                    .onField("$SUB.$SUB_TITLE")
                    .partial(PartialIndexFilter.of(where("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.name").`is`(TITLE.value)))
                    .onField("$SUB.$SUB_SECTION.$SUB_ATTRIBUTES.value")
                    .build()
            )
        }
    }
}

@ChangeLog
class ChangeLog003 {
    @ChangeSet(order = "003", id = "Submission Modification time", author = "System")
    fun changeSet003(template: MongockTemplate) {
        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(Index().on(SUB_MODIFICATION_TIME, DESC))
        }

        template.indexOps(DocSubmissionRequest::class.java).apply {
            ensureIndex(Index().on("$SUB.$SUB_MODIFICATION_TIME", DESC))
        }
    }
}

@ChangeLog
class ChangeLog004 {
    @ChangeSet(order = "004", id = "Submission fields indexes in FileListDocFile", author = "System")
    fun changeSet004(template: MongockTemplate) {
        template.ensureExists(FileListDocFile::class.java)

        template.indexOps(FileListDocFile::class.java).apply {
            ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_ID, ASC))
            ensureIndex(Index().on(FILE_LIST_DOC_FILE_FILE_LIST_NAME, ASC))
            ensureIndex(Index().on(FILE_LIST_DOC_FILE_INDEX, ASC))
            ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC))
            ensureIndex(Index().on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC))
        }
    }
}

@ChangeLog
class ChangeLog005 {
    @ChangeSet(order = "005", id = "Set ACTIVE status on existing Drafts", author = "System")
    fun changeSet005(template: MongockTemplate) {
        template.updateMulti(Query(), Update().set(STATUS, ACTIVE.name), DocSubmissionDraft::class.java)
    }
}

@ChangeLog
class ChangeLog006 {
    @ChangeSet(order = "006", id = "Extract stats to a separated collection", author = "System")
    fun changeSet006(template: MongockTemplate) {
        template.ensureExists(DocSubmissionStats::class.java)

        template.indexOps(DocSubmissionStats::class.java).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
        }

        template.updateMulti(Query(), Update().unset(SUB_STATS), DocSubmission::class.java)
    }
}

@ChangeLog
class ChangeLog007 {
    @ChangeSet(order = "007", id = "Create submission request files collection", author = "System")
    fun changeSet007(template: MongockTemplate) {
        template.ensureExists(DocSubmissionRequestFile::class.java)

        template.indexOps(DocSubmissionRequestFile::class.java).apply {
            ensureIndex(Index().on(RQT_FILE_INDEX, ASC))
            ensureIndex(Index().on(RQT_FILE_PATH, ASC))
            ensureIndex(Index().on(RQT_FILE_SUB_ACC_NO, ASC))
            ensureIndex(Index().on(RQT_FILE_SUB_VERSION, ASC))
        }
    }
}

@ChangeLog
class ChangeLog008 {
    @ChangeSet(order = "008", id = "File List files index", author = "System")
    fun changeSet007(template: MongockTemplate) {
        template.indexOps(FileListDocFile::class.java).apply {
            ensureIndex(
                Index()
                    .on(FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO, ASC)
                    .on(FILE_LIST_DOC_FILE_SUBMISSION_VERSION, ASC)
                    .on("$FILE_LIST_DOC_FILE_FILE.${DocFileFields.FILE_DOC_FILEPATH}", ASC)
            )
        }
    }
}
