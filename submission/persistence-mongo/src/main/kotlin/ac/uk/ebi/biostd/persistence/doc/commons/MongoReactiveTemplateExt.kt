package ac.uk.ebi.biostd.persistence.doc.commons

import com.mongodb.reactivestreams.client.MongoCollection
import org.bson.Document
import org.springframework.data.mongodb.core.FindAndReplaceOptions
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Query

fun <T> ReactiveMongoTemplate.replaceOrCreate(query: Query, replacement: T): T =
    findAndReplace(query, replacement, FindAndReplaceOptions().upsert().returnNew()).block()!!

inline fun <reified T : Any> ReactiveMongoOperations.collection(): MongoCollection<Document> {
    return getCollection(getCollectionName(T::class.java)).block()!!
}
