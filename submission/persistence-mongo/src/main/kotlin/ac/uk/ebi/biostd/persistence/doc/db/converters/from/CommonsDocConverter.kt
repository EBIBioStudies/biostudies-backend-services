package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import org.bson.Document

fun Document.getDocList(key: String): List<Document> = getList(key, documentClass, listOf())
fun Document.getDoc(key: String): Document = get(key, documentClass)
fun Document.findDoc(key: String): Document? = get(key, documentClass)
private val documentClass = Document::class.java
