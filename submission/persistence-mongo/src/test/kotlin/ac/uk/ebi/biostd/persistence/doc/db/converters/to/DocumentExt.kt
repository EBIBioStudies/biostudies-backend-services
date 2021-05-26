package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import org.bson.Document

inline fun <reified T> Document.getAs(field: String): T {
    return get(field) as T
}
