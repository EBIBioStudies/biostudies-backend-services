package ac.uk.ebi.pmc.migrations

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.data.mongodb.core.ReactiveMongoOperations

/**
 * Ensure that a collection for the provided clazz exists. If not it will be created.
 *
 * @param clazz must not be null.
 */
suspend fun ReactiveMongoOperations.ensureExists(collectionName: String) {
    if (collectionExists(collectionName).awaitSingle().not()) createCollection(collectionName)
}
