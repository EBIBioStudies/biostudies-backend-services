package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.commons.ensureExists
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ATTRIBUTES
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_OWNER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionDraft
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionRequest
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import com.mongodb.BasicDBObject
import ebi.ac.uk.model.constants.SectionFields.TITLE
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.domain.Sort.Direction.DESC
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.PartialIndexFilter
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

internal const val TITLE_INDEX_NAME = "title_index"

@ChangeLog
class DatabaseChangeLog {
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

    @ChangeSet(order = "002", id = "Section Title Index", author = "System")
    fun changeSet002(template: MongockTemplate) {
        template.ensureExists(DocSubmission::class.java)
        template.ensureExists(DocSubmissionRequest::class.java)

        template.indexOps(DocSubmission::class.java).apply {
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

    @ChangeSet(order = "003", id = "Submission Modification time", author = "System")
    fun changeSet003(template: MongockTemplate) {
        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(Index().on(SUB_MODIFICATION_TIME, DESC))
        }

        template.indexOps(DocSubmissionRequest::class.java).apply {
            ensureIndex(Index().on("$SUB.$SUB_MODIFICATION_TIME", DESC))
        }
    }

    @ChangeSet(order = "004", id = "Set ACTIVE status on existing Drafts", author = "System")
    fun changeSet004(template: MongockTemplate) {
        val collectionName = template.getCollectionName(DocSubmissionDraft::class.java)
        val query = BasicDBObject()
        query.put("statusDraft", "ACTIVE")
        val updateObject = BasicDBObject()
        updateObject.put("\$set", query)
        template.getCollection(collectionName).updateMany(BasicDBObject(), updateObject)
    }
}
