package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.tsv.deserialization.model.FileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.FileTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LibFileChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinkChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.SectionTableChunk
import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunk
import ac.uk.ebi.biostd.validation.SerializationError
import ac.uk.ebi.biostd.validation.SerializationException
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission

@Suppress("TooManyFunctions")
class TsvSerializationContext {

    private val sections: MutableMap<String, Section> = mutableMapOf()
    private var errors: Multimap<Any, SerializationError> = HashMultimap.create()

    private var submission: Submission = Submission()
    private var rootSection: Section = Section()
    private var currentSection: Section = rootSection

    fun getSubmission() = if (errors.isEmpty) submission else throw SerializationException(submission, errors)

    fun addSubmission(chunk: TsvChunk, function: (TsvChunk) -> Submission) =
        execute(submission, chunk) { submission = function(chunk) }

    fun addRootSection(chunk: TsvChunk, function: (TsvChunk) -> Section) {
        execute(submission, chunk) { rootSection = function(chunk) }
        submission.section = addSection(rootSection)
    }

    fun addLink(chunk: LinkChunk) = execute(currentSection, chunk) { currentSection.addLink(chunk.asLink()) }

    fun addFile(chunk: FileChunk) = execute(currentSection, chunk) { currentSection.addFile(chunk.asFile()) }

    fun addLinksTable(chunk: LinksTableChunk) =
        execute(currentSection, chunk) { currentSection.addLinksTable(chunk.asTable()) }

    fun addFilesTable(chunk: FileTableChunk) =
        execute(currentSection, chunk) { currentSection.addFilesTable(chunk.asTable()) }

    fun setLibraryFile(chunk: LibFileChunk) =
        execute(currentSection, chunk) { currentSection.libraryFile = chunk.asLibraryFile() }

    fun addSectionTable(chunk: SectionTableChunk) =
        execute(rootSection, chunk) { rootSection.addSectionTable(chunk.asTable()) }

    fun addSubSectionTable(parent: String, chunk: SectionTableChunk) =
        execute(rootSection, chunk) { sections.getValue(parent).addSectionTable(chunk.asTable()) }

    fun addSection(chunk: SectionChunk) =
        execute(rootSection, chunk) { addSection(rootSection, chunk.asSection()) }

    fun addSubSection(parent: String, chunk: SectionChunk) =
        execute(rootSection, chunk) { addSection(sections.getValue(parent), chunk.asSection()) }

    private fun addSection(parent: Section, section: Section) = parent.addSection(addSection(section))

    private fun addSection(section: Section): Section {
        section.accNo?.let { sections[it] = section }
        currentSection = section
        return currentSection
    }

    @Suppress("TooGenericExceptionCaught")
    private fun <T> execute(parent: T, chunk: TsvChunk, function: (TsvChunk) -> Unit) {
        try {
            function(chunk)
        } catch (exception: Exception) {
            errors.put(parent, SerializationError(chunk, exception))
        }
    }
}
