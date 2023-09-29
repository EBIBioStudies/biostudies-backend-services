package ac.uk.ebi.biostd.persistence.doc.commons

import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoOperations

/**
 * Ensure that a collection for the provided clazz exists. If not it will be created.
 *
 * @param clazz must not be null.
 */
suspend fun <T> ReactiveMongoOperations.ensureExists(clazz: Class<T>) {
    if (collectionExists(clazz).awaitSingle().not()) createCollection(clazz)
}
