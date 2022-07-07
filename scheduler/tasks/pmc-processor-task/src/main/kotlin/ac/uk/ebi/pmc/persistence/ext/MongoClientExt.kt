package ac.uk.ebi.pmc.persistence.ext

import com.mongodb.reactivestreams.client.MongoClient

inline fun <reified T> MongoClient.getCollection(dataBase: String, name: String) =
    getDatabase(dataBase).getCollection(name, T::class.java)
