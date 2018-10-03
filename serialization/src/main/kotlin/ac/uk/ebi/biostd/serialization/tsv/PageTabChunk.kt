package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.submission.Attribute

const val TSV_SEPARATOR: String = "\t"

data class PageTabChunk(
    val header: String,
    val attributes: MutableList<Attribute>) {

    constructor(body: MutableList<String>): this(body.removeAt(0), mutableListOf()) {
        body.forEach {
            attributes.add(Attribute(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)))
        }
    }

    fun getHeaderValues(): List<String> {
        return header.split(TSV_SEPARATOR)
    }

    fun <T> mapTable(initializer: (String, MutableList<Attribute>) -> T): List<T> {
        val head: List<String> = getHeaderValues()
        val rows: MutableList<T> = mutableListOf()

        attributes.forEach {
            val attrs: MutableList<Attribute> = mutableListOf()
            it.value.split(TSV_SEPARATOR).forEachIndexed { index, attr -> attrs.add(Attribute(head[index + 1], attr)) }
            rows.add(initializer(it.name, attrs))
        }

        return rows.toList()
    }
}
