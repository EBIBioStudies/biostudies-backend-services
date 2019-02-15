package ac.uk.ebi.pmc.persistence.ext

import com.mongodb.async.client.MongoCollection
import com.mongodb.client.model.FindOneAndUpdateOptions
import org.bson.BsonDocument
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.findOne
import org.litote.kmongo.coroutine.singleResult

suspend fun <T> MongoCollection<T>.getOne(filter: Bson) = findOne(filter)!!

suspend fun <T : Any> MongoCollection<T>.findOneAndUpdate(
    filter: BsonDocument,
    update: BsonDocument,
    options: FindOneAndUpdateOptions = FindOneAndUpdateOptions()
): T? {
    return singleResult { findOneAndUpdate(filter, update, options, it) }
}
