package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import org.bson.Document

fun Document.getDocList(key: String): List<Document> = getList(key, Document::class.java, listOf())

fun Document.getDoc(key: String): Document = get(key, Document::class.java)

fun Document.findDoc(key: String): Document? = get(key, Document::class.java)
