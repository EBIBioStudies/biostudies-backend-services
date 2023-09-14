package ac.uk.ebi.biostd.persistence.doc.commons

import org.springframework.data.mongodb.core.MongoOperations

/**
 * Ensure that a collection for the provided clazz exists. If not it will be created.
 *
 * @param clazz must not be null.
 */
fun <T> MongoOperations.ensureExists(clazz: Class<T>) {
    if (collectionExists(clazz).not()) createCollection(clazz)
}
