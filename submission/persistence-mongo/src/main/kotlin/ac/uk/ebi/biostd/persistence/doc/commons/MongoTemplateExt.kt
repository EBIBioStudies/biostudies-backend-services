package ac.uk.ebi.biostd.persistence.doc.commons

import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

fun <T> MongoTemplate.replaceOrCreate(query: Query, replacement: T): T =
    findAndReplace(query, replacement, FindAndReplaceOptions().upsert().returnNew())!!
