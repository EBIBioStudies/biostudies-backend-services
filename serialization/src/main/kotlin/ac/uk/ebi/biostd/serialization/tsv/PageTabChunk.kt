package ac.uk.ebi.biostd.serialization.tsv

import ac.uk.ebi.biostd.serialization.common.TSV_SEPARATOR
import ac.uk.ebi.biostd.submission.Attribute

data class PageTabChunk(
    val header: List<String>,
    val attributes: MutableList<Attribute>) {

    constructor(body: MutableList<String>): this(body.removeAt(0).split(TSV_SEPARATOR), mutableListOf()) {
        body.forEach {
            attributes.add(Attribute(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)))
        }
    }

    fun getType(): String = header[0]

    fun getIdentifier(): String = header[1]

    fun <T> mapTable(initializer: (String, MutableList<Attribute>) -> T): List<T> {
        val rows: MutableList<T> = mutableListOf()

        attributes.forEach {
            val attrs: MutableList<Attribute> = mutableListOf()
            it.value.split(TSV_SEPARATOR).forEachIndexed { index, attr -> attrs.add(Attribute(header[index + 1], attr)) }
            rows.add(initializer(it.name, attrs))
        }

        return rows.toList()
    }
}
