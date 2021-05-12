package ac.uk.ebi.biostd.persistence.doc.commons

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate

fun <T> MongockTemplate.ensureExists(clazz: Class<T>) {
    if (collectionExists(clazz).not()) createCollection(clazz)
}
