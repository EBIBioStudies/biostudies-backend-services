package ac.uk.ebi.biostd.persistence.doc.commons

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate

/**
 * Ensure that a collection for the provided clazz exists. If not it will be created.
 *
 * @param clazz must not be null.
 */
fun <T> MongockTemplate.ensureExists(clazz: Class<T>) {
    if (collectionExists(clazz).not()) createCollection(clazz)
}
