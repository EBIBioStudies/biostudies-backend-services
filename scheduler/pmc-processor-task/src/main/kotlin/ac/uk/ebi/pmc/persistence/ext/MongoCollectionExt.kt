package ac.uk.ebi.pmc.persistence.ext

import com.mongodb.async.client.MongoCollection
import org.bson.conversions.Bson
import org.litote.kmongo.coroutine.findOne

suspend fun <T> MongoCollection<T>.getOne(filter: Bson) = findOne(filter)!!
