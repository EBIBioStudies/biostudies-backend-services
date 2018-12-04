package ac.uk.ebi.biostd.tsv.deserialization.model

import ac.uk.ebi.biostd.tsv.TSV_SEPARATOR

data class TsvChunk(
    val header: List<String>,
    val lines: MutableList<TsvChunkLine>
) {
    constructor(body: MutableList<String>) : this(
        body.removeAt(0).split(TSV_SEPARATOR),
        body.mapTo(mutableListOf()) { TsvChunkLine(it.substringBefore(TSV_SEPARATOR), it.substringAfter(TSV_SEPARATOR)) })
}
