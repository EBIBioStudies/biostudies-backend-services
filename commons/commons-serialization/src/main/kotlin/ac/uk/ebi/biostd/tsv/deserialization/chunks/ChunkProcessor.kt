package ac.uk.ebi.biostd.tsv.deserialization.chunks

import ac.uk.ebi.biostd.tsv.deserialization.TsvSerializationContext
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
import ebi.ac.uk.model.constants.SubFields.SUBMISSION

internal class ChunkProcessor {
    fun getSubmission(tsvChunk: TsvChunk): Submission {
        validate(tsvChunk.getType() like SUBMISSION) { "Expected to find block type of $SUBMISSION" }

        return Submission(
            accNo = tsvChunk.findId().orEmpty(),
            attributes = toAttributes(tsvChunk.lines)
        )
    }

    fun getRootSection(tsvChunk: TsvChunk) = Section(
        accNo = tsvChunk.findId(),
        type = tsvChunk.getTypeOrElse(InvalidElementException(REQUIRED_ROOT_SECTION)),
        attributes = toAttributes(tsvChunk.lines))

    inline fun <reified T> processIsolatedChunk(chunk: TsvChunk) = when (chunk) {
        is LinkChunk -> chunk.asLink() as T
        is FileChunk -> chunk.asFile() as T
        is LinksTableChunk -> chunk.asTable() as T
        is FileTableChunk -> chunk.asTable() as T
        is SectionChunk -> TODO("Implement section chunk isolated deserialization")
        is SectionTableChunk -> TODO("Implement section table chunk isolated deserialization")
    }

    fun processChunk(chunk: TsvChunk, sectionContext: TsvSerializationContext) {
        when (chunk) {
            is LinkChunk -> sectionContext.addLink(chunk)
            is FileChunk -> sectionContext.addFile(chunk)
            is LinksTableChunk -> sectionContext.addLinksTable(chunk)
            is FileTableChunk -> sectionContext.addFilesTable(chunk)
            is SectionTableChunk -> processSectionTable(chunk, sectionContext)
            is SectionChunk -> processSection(chunk, sectionContext)
        }
    }

    private fun processSection(chunk: SectionChunk, sectionContext: TsvSerializationContext) {
        when (chunk) {
            is RootSubSectionChunk -> sectionContext.addSection(chunk)
            is SubSectionChunk -> sectionContext.addSubSection(chunk.parent, chunk)
        }
    }

    private fun processSectionTable(chunk: SectionTableChunk, sectionContext: TsvSerializationContext) {
        when (chunk) {
            is RootSectionTableChunk -> sectionContext.addSectionTable(chunk)
            is SubSectionTableChunk -> sectionContext.addSubSectionTable(chunk.parent, chunk)
        }
    }
}
