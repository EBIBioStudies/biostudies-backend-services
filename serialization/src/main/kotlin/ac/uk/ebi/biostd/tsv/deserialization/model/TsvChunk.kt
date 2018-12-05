package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.tsv.TSV_SEPARATOR
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.secondOrElse
import ebi.ac.uk.util.collections.thirdOrElse

class TsvChunk(body: MutableList<String>) {

    val header: TsvChunkLine
    val lines: List<TsvChunkLine>

    init {
        val lines = body.map { TsvChunkLine(it) }
        this.header = lines.first()
        this.lines = lines.drop(1)
    }
}

class TsvChunkLine(body: String) {

    private val lines: List<String> = body.split(TSV_SEPARATOR).filter(String::isNotBlank)

    fun first() = lines.first()
    fun secondOrElse(defaultValue: String?) = lines.secondOrElse(defaultValue)
    fun secondOrElse(function: () -> String) = lines.secondOrElse(function)
    fun thirdOrElse(defaultValue: String): String = lines.thirdOrElse(defaultValue)

    operator fun get(index: Int) = lines[index]

    val value: String
        get() {
            return lines.second()
        }

    val values: List<String>
        get() {
            return lines.drop(1)
        }

    val name: String
        get() {
            return lines.first()
        }
}
