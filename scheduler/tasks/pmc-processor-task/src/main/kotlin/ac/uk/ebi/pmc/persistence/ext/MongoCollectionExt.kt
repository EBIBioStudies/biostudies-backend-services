package ac.uk.ebi.pmc.persistence.ext

import com.mongodb.reactivestreams.client.MongoCollection
import kotlinx.coroutines.reactive.awaitFirst
import org.bson.conversions.Bson

suspend fun <T> MongoCollection<T>.getOne(filter: Bson): T = find(filter).first().awaitFirst()!!

suspend fun <T> MongoCollection<T>.findOne(filter: Bson): T? = find(filter).first().awaitFirst()
