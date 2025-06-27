package ac.uk.ebi.biostd.tsv.deserialization.chunks

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.tsv.TSV_COMMENT
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import com.google.common.collect.Lists
import ebi.ac.uk.base.like
import ebi.ac.uk.base.nullIfBlank
import ebi.ac.uk.base.scape
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.model.constants.TABLE_REGEX
import ebi.ac.uk.util.collections.findThird
import ebi.ac.uk.util.collections.split
import ebi.ac.uk.util.regex.findGroup
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.io.StringReader
import java.util.Queue

internal class TsvChunkGenerator(
    private val parser: CSVFormat = createParser(),
) {
    fun chunks(pageTab: String): Queue<TsvChunk> =
        chunkLines(pageTab)
            .split { it.isEmpty() }
            .mapTo(Lists.newLinkedList()) { createChunk(it) }

    private fun createChunk(lines: List<TsvChunkLine>): TsvChunk {
        val header = lines.first()
        val type = header.first()

        return when {
            type == null -> error("a type is required")
            type like LinkFields.LINK -> LinkChunk(lines)
            type like FileFields.FILE -> FileChunk(lines)
            type like SectionFields.LINKS -> LinksTableChunk(lines)
            type like SectionFields.FILES -> FileTableChunk(lines)
            type.matches(TABLE_REGEX) ->
                when (val group = TABLE_REGEX.findGroup(type, 1)) {
                    null -> RootSectionTableChunk(lines)
                    else -> SubSectionTableChunk(lines, group)
                }

            else -> header.findThird()?.let { SubSectionChunk(lines, it) } ?: RootSubSectionChunk(lines)
        }
    }

    private fun chunkLines(pageTab: String): List<TsvChunkLine> {
        /**
         * Change scape character in records values (values defined tab limited). If value is an empty String is
         * replaced with null.
         */
        fun processRecord(record: String): String? =
            record
                .replace(ESCAPED_QUOTE, SIMPLE_QUOTE)
                .nullIfBlank()

        val parsedChunks = parser.parse(StringReader(escapeQuotes(pageTab)))
        return parsedChunks.mapIndexed { idx, csvRecord ->
            val record = csvRecord.asList()
            when {
                record.all(String::isBlank) -> TsvChunkLine(idx, emptyList())
                else -> TsvChunkLine(idx, record.map { processRecord(it) })
            }
        }
    }

    private fun escapeQuotes(pageTab: String): String =
        SIMPLE_QUOTE_REGEX
            .findAll(pageTab)
            .fold(pageTab) { result, match -> result.replace(match.value, match.value.scape(QUOTE)) }

    private fun CSVRecord.asList(): List<String> = map { it }

    companion object {
        private val SIMPLE_QUOTE_REGEX = "(\")([^\n|\t|\"]*)(\")".toRegex()
        private const val QUOTE = "\""
        private const val ESCAPED_QUOTE = "\\\""
        private const val SIMPLE_QUOTE = "\""

        private fun createParser(): CSVFormat =
            CSVFormat.DEFAULT
                .withDelimiter(TAB)
                .withQuote('"')
                .withIgnoreSurroundingSpaces()
                .withIgnoreEmptyLines(false)
                .withCommentMarker(TSV_COMMENT)
    }
}
