package ac.uk.ebi.biostd.persistence.doc.migrations

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSectionFields.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASED
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_SECTION
import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields.SUB_TITLE
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.SubmissionRequest
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.index.Index
import kotlin.reflect.KClass

@ChangeLog
class DatabaseChangeLog {

    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun createSchema(template: MongockTemplate) {
        if (!template.collectionExists(DocSubmission::class.java)) {
            template.createCollection(DocSubmission::class.java)
        }
        if (!template.collectionExists(SubmissionRequest::class.java)) {
            template.createCollection(SubmissionRequest::class.java)
        }

        template.indexOps(DocSubmission::class.java).apply {
            ensureIndex(Index().on("accNo", ASC))
            ensureIndex(Index().on("accNo", ASC).on("version", ASC))
            ensureIndex(Index().on("$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on(SUB_RELEASE_TIME, ASC))
            ensureIndex(Index().on(SUB_TITLE, ASC))
            ensureIndex(Index().on(SUB_RELEASED, ASC))
        }

        template.indexOps(SubmissionRequest::class.java).apply {
            ensureIndex(Index().on("accNo", ASC))
            ensureIndex(Index().on("accNo", ASC).on("version", ASC))
            ensureIndex(Index().on("submission.$SUB_SECTION.$SEC_TYPE", ASC))
            ensureIndex(Index().on("submission.$SUB_ACC_NO", ASC))
            ensureIndex(Index().on("submission.$SUB_RELEASE_TIME", ASC))
            ensureIndex(Index().on("submission.$SUB_TITLE", ASC))
            ensureIndex(Index().on("submission.$SUB_RELEASED", ASC))
        }
    }
    
    companion object {
        private inline fun <reified T : KClass<Any>> T.createCollectionIfNotExists(template: MongockTemplate) {
            if (!template.collectionExists(T::class.java)) {
                template.createCollection(T::class.java)
            }
        }
    }
}
