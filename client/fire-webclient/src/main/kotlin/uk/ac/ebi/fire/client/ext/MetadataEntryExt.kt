package uk.ac.ebi.fire.client.ext

import uk.ac.ebi.fire.client.model.MetadataEntry

internal fun List<MetadataEntry>.asRequestParameter(): String = "{ ${joinToString(",") { it.toString() }} }"
