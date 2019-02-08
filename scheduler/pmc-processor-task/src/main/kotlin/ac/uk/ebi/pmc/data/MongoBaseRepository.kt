package ac.uk.ebi.pmc.data

import com.mongodb.async.client.MongoClient

open class MongoBaseRepository(
    val dataBase: String,
    val mongoClient: MongoClient
) {

    protected inline fun <reified T> getCollection(name: String) =
        mongoClient.getDatabase(dataBase).getCollection(name, T::class.java)!!
}
