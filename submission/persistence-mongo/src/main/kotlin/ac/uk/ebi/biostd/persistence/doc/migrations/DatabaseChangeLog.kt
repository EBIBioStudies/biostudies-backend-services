package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.commons.ensureExists
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_VERSION
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.index.TextIndexDefinition.builder as TextIndex

@ChangeLog
class DatabaseChangeLog {

    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun createSchema(template: MongockTemplate) {
        template.ensureExists(DocSubmission::class.java)
        template.ensureExists(SubmissionRequest::class.java)

        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC))
            ensureIndex(Index().on("$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on(SUB_RELEASE_TIME, ASC))
            ensureIndex(TextIndex().onField(SUB_TITLE).build())
            ensureIndex(Index().on(SUB_RELEASED, ASC))
        }

        template.indexOps(SubmissionRequest::class.java).apply {
            ensureIndex(Index().on(SUB_ACC_NO, ASC))
            ensureIndex(Index().on(SUB_ACC_NO, ASC).on(SUB_VERSION, ASC))
            ensureIndex(Index().on("submission.$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on("submission.$SUB_ACC_NO", ASC))
            ensureIndex(Index().on("submission.$SUB_RELEASE_TIME", ASC))
            ensureIndex(TextIndex().onField("submission.$SUB_TITLE").build())
            ensureIndex(Index().on("submission.$SUB_RELEASED", ASC))
        }
    }
}
