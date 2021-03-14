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
import com.univocity.parsers.csv.CsvParser
import com.univocity.parsers.csv.CsvParserSettings
import ebi.ac.uk.base.EMPTY
import ebi.ac.uk.base.like
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.SectionFields
import ebi.ac.uk.model.constants.TABLE_REGEX
import ebi.ac.uk.util.collections.findThird
import ebi.ac.uk.util.collections.split
import ebi.ac.uk.util.regex.findGroup
import java.io.StringReader
import java.util.Queue

internal class TsvChunkGenerator(private val parser: CsvParser = createParser()) {
    fun chunks(pageTab: String): Queue<TsvChunk> {
        return chunkLines(pageTab)
            .split { it.isEmpty() }
            .mapTo(Lists.newLinkedList()) { createChunk(it) }
    }

    private fun createChunk(body: List<TsvChunkLine>): TsvChunk {
        val header = body.first()
        val type = header.first()

        return when {
            type like LinkFields.LINK -> LinkChunk(body)
            type like FileFields.FILE -> FileChunk(body)
            type like SectionFields.LINKS -> LinksTableChunk(body)
            type like SectionFields.FILES -> FileTableChunk(body)
            type.matches(TABLE_REGEX) -> TABLE_REGEX.findGroup(type, 1)
                .fold({ RootSectionTableChunk(body) }, { SubSectionTableChunk(body, it) })
            else -> header.findThird().fold({ RootSubSectionChunk(body) }, { SubSectionChunk(body, it) })
        }
    }

    private fun chunkLines(pageTab: String): List<TsvChunkLine> {
        val parsedChunks = parser.parseAll(StringReader(pageTab))
        return parsedChunks.mapIndexed(this::asTsvChunkLine)
    }

    private fun asTsvChunkLine(idx: Int, values: Array<String?>): TsvChunkLine =
        if (values.all { it.isNullOrBlank() }) TsvChunkLine(idx) else TsvChunkLine(idx, asList(values))

    private fun asList(values: Array<String?>) = values.map { it.orEmpty() }.toList()

    companion object {
        private fun createParser(): CsvParser {
            val settings = CsvParserSettings()
            settings.format.setLineSeparator("\n")
            settings.format.delimiter = TAB
            settings.format.comment = TSV_COMMENT
            settings.skipEmptyLines = false
            return CsvParser(settings)
        }
    }
}
