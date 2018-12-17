package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.deserialization.common.findId
import ac.uk.ebi.biostd.tsv.deserialization.common.getType
import ac.uk.ebi.biostd.tsv.deserialization.common.getTypeOrElse
import ac.uk.ebi.biostd.tsv.deserialization.common.toAttributes
import ac.uk.ebi.biostd.tsv.deserialization.common.validate
import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.RootSubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SubSectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ROOT_SECTION
import ebi.ac.uk.base.like
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.constans.SubFields

private const val ALLOWED_TYPES = "Study"

class ChunkProcessor {

    fun getSubmission(tsvChunk: TsvChunk): Submission {
        validate(tsvChunk.getType() like SubFields.SUBMISSION) { "Expected to find block type of ${SubFields.SUBMISSION}" }

        return Submission(
            accNo = tsvChunk.findId().orEmpty(),
            attributes = toAttributes(tsvChunk.lines)
        )
    }

    fun getRootSection(tsvChunk: TsvChunk): Section {
        val type = tsvChunk.getTypeOrElse(InvalidElementException(REQUIRED_ROOT_SECTION))
        validate(type in ALLOWED_TYPES) { "Expected to find block type of $type" }

        return Section(
            accNo = tsvChunk.findId(),
            type = type,
            attributes = toAttributes(tsvChunk.lines))
    }

    fun processChunk(chunk: TsvChunk, sectionContext: TsvSerializationContext) {
        when (chunk) {
            is LinkChunk -> sectionContext.addLink { chunk.asLink() }
            is FileChunk -> sectionContext.addFile { chunk.asFile() }
            is LinksTableChunk -> sectionContext.addLinksTable { chunk.asTable() }
            is FileTableChunk -> sectionContext.addFilesTable { chunk.asTable() }
            is SectionTableChunk -> {
                when (chunk) {
                    is RootSectionTableChunk -> sectionContext.addSectionTable { chunk.asTable() }
                    is SubSectionTableChunk -> sectionContext.addSubSectionTable(chunk.parent) { chunk.asTable() }
                }
            }
            is SectionChunk -> {
                when (chunk) {
                    is RootSubSectionChunk -> sectionContext.addSection { chunk.asSection() }
                    is SubSectionChunk -> sectionContext.addSubSection(chunk.parent) { chunk.asSection() }
                }
            }
        }
    }
}
