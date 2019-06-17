package ac.uk.ebi.biostd.tsv

internal const val TSV_SEPARATOR = "\t"
internal val TSV_CHUNK_BREAK = "\r\n|\n".toRegex()
internal const val TSV_COMMENT = "#"
internal const val SECTION_TABLE_OP = "["
