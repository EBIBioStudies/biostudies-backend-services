package ac.uk.ebi.pmc.load

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.tsv.deserialization.chunks.TsvChunkGenerator
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ebi.ac.uk.base.like
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constants.LinkFields.LINK
import ebi.ac.uk.model.constants.SectionFields.LINKS
import ebi.ac.uk.model.constants.SubFields.RELEASE_DATE
import ebi.ac.uk.model.constants.SubFields.SUBMISSION
import ebi.ac.uk.util.regex.getGroup
import java.time.LocalDate

/**
 * Performs the pagetab transformations required to update PMC generated representation.
 */
class PmcSubmissionTabProcessor(
    private val serializationService: SerializationService,
) {
    fun transformSubmission(originalPagetab: String): Submission {
        val chunks = TsvChunkGenerator().chunks(originalPagetab)
        val releaseDate = getReleaseDate(chunks.toList())
        val pagetab = chunks.joinToString("\n\n") { transformChunk(it, releaseDate) }
        return serializationService.deserializeSubmission(pagetab, SubFormat.TSV)
    }

    private fun getReleaseDate(chunks: List<TsvChunk>): String {
        val publication = chunks.first { it.header.name == PUBLICATION_SECTION }
        val releaseDate = publication.lines.first { it.name == PUBLICATION_DATE_ATTRIBUTE }.value!!
        val year = yearExtractionPattern.getGroup(releaseDate).toInt()
        return LocalDate.ofYearDay(year, 1).toString()
    }

    private fun transformChunk(
        chunk: TsvChunk,
        releaseDate: String,
    ): String {
        fun processSubmissionChunks(lines: List<TsvChunkLine>) =
            lines
                .map { line ->
                    if (line.name.orEmpty() like RELEASE_DATE) listOf(line.name, releaseDate) else line.toList()
                }.let { lines ->
                    if (chunk.lines.any { it.name.orEmpty() like RELEASE_DATE }) {
                        lines
                    } else {
                        lines + listOf(listOf(RELEASE_DATE.value, releaseDate))
                    }
                }

        fun processLinkChunks(lines: List<TsvChunkLine>) =
            lines.map { line ->
                line.mapIndexed { index, value ->
                    if (index == 1 && !isAttrDetail(line.name.orEmpty())) normalizeLinkValue(value) else value
                }
            }

        fun processLinkTablesChunks(lines: List<TsvChunkLine>) =
            lines.map { line ->
                line.mapIndexed { index, value ->
                    val header = chunk.header.getOrNull(index).orEmpty()
                    if (index > 0 && !isAttrDetail(header)) normalizeLinkValue(value) else value
                }
            }

        val type = chunk.header.name.orEmpty()
        val lines =
            when {
                type like SUBMISSION -> {
                    processSubmissionChunks(chunk.lines)
                }

                type like LINK -> {
                    processLinkChunks(chunk.lines)
                }

                type like LINKS -> {
                    processLinkTablesChunks(chunk.lines)
                }

                else -> {
                    chunk.lines.map { it.toList() }
                }
            }

        val chunks = listOf(chunk.header.toList()) + lines
        return chunks.joinToString("\n") { it.joinToString("\t", transform = this::formatValue) }
    }

    private fun formatValue(value: String?): String =
        when {
            value == null -> ""
            value.any { it == '\n' || it == '\t' } -> "\"$value\""
            else -> value
        }

    private fun isAttrDetail(name: String) = name.matches(attributeDetailPattern)

    private fun normalizeLinkValue(value: String?): String? = if (value == GEN_TYPE_LINK) ENA_TYPE_LINK else value

    private companion object {
        const val PUBLICATION_SECTION = "Publication"
        const val PUBLICATION_DATE_ATTRIBUTE = "Publication date"
        const val GEN_TYPE_LINK = "gen"
        const val ENA_TYPE_LINK = "ENA"

        val yearExtractionPattern = "\\d{4}".toRegex()
        val attributeDetailPattern = "\\(.+\\)|\\[.+\\]".toRegex()
    }
}
