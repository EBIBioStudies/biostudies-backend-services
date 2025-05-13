package ac.uk.ebi.biostd.persistence.doc.commons

import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactor.awaitSingle
import org.bson.Document
import org.springframework.data.mongodb.core.ReactiveMongoOperations

suspend inline fun <reified T : Any> ReactiveMongoOperations.collection(): MongoCollection<Document> =
    getCollection(getCollectionName(T::class.java)).awaitSingle()
