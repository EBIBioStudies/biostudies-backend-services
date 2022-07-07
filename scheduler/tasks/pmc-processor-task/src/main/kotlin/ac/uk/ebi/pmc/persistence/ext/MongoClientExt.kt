package ac.uk.ebi.pmc.persistence.ext

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection

inline fun <reified T> MongoClient.getCollection(dataBase: String, name: String): MongoCollection<T> =
    getDatabase(dataBase).getCollection(name, T::class.java)
