package ac.uk.ebi.pmc.data.ext

import com.mongodb.async.client.MongoClient

inline fun <reified T> MongoClient.getCollection(dataBase: String, name: String) =
    getDatabase(dataBase).getCollection(name, T::class.java)
