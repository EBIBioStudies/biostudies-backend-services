package ac.uk.ebi.biostd.persistence.doc.commons

import com.mongodb.client.MongoCollection
import org.bson.Document
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.getCollectionName

inline fun <reified T : Any> MongoOperations.collection(): MongoCollection<Document> =
    getCollection(getCollectionName<T>())
