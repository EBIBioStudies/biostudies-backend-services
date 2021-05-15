package ac.uk.ebi.pmc.migrations

import ac.uk.ebi.pmc.persistence.docs.FileDoc
import ac.uk.ebi.pmc.persistence.docs.InputFileDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionDoc
import ac.uk.ebi.pmc.persistence.docs.SubmissionErrorDoc
import com.github.cloudyrock.mongock.ChangeLog
import com.github.cloudyrock.mongock.ChangeSet
import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate
import com.mongodb.MongoNamespace
import org.springframework.data.domain.Sort
import org.springframework.data.domain.Sort.Direction.ASC
import org.springframework.data.mongodb.core.CollectionOptions
import org.springframework.data.mongodb.core.index.Index

const val databaseName = "dataBaseName"

@ChangeLog
class PMCDatabaseChangeLog {

    @ChangeSet(order = "001", id = "Create Schema", author = "System")
    fun createSchema(template: MongockTemplate) {
        template.createCollectionIfNotExistsWithName(databaseName, "pmc_errors", SubmissionErrorDoc::class.java)
        template.createCollectionIfNotExistsWithName(databaseName, "pmc_submissions", SubmissionDoc::class.java)
        template.createCollectionIfNotExistsWithName(databaseName, "pmc_submissions_files", FileDoc::class.java)
        template.createCollectionIfNotExistsWithName(databaseName, "pmc_input_files", InputFileDoc::class.java)

        template.indexOps(SubmissionErrorDoc::class.java).apply {
            ensureIndex(Index().on("sourceFile", ASC))
            ensureIndex(Index().on("mode", ASC))
            ensureIndex(Index().on("accNo", ASC))
            ensureIndex(Index().on("uploaded", ASC))
        }
        template.indexOps(SubmissionDoc::class.java).apply {
            ensureIndex(Index().on("accNo", ASC))
            ensureIndex(Index().on("accNo", ASC).on("sourceTime", ASC))
            ensureIndex(Index().on("accNo", ASC).on("sourceTime", ASC).on("posInFile", ASC))
            ensureIndex(Index().on("status", ASC))
            ensureIndex(Index().on("sourceFile", ASC))
            ensureIndex(Index().on("sourceTime", ASC))
//            added
//            ensureIndex(Index().on("status", ASC).on("updated", ASC))
//            ensureIndex(Index().on("sourceTime",ASC).on("posInFile",ASC))
        }
        template.indexOps(FileDoc::class.java).apply {
            ensureIndex(Index().on("_id", ASC))
            ensureIndex(Index().on("accNo", ASC))
            ensureIndex(Index().on("path", ASC))
        }
    }
    private fun <T> MongockTemplate.createCollectionIfNotExistsWithName(
        collectionName: String,
        databaseName: String,
        clazz: Class<T>
    ) {
        if (collectionExists(collectionName).not()) createCollection(clazz).renameCollection(
            MongoNamespace(
                databaseName,
                collectionName
            )
        )
    }
}
