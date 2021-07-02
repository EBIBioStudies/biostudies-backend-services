package ac.uk.ebi.pmc.migrations

import com.github.cloudyrock.mongock.driver.mongodb.springdata.v3.decorator.impl.MongockTemplate

fun MongockTemplate.createCollectionByNameIfNotExists(
    collectionName: String,
) {
    if (collectionExists(collectionName).not()) createCollection(collectionName)
}
