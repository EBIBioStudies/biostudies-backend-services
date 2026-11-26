package uk.ac.ebi.biostd.tsv.deserialization

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import uk.ac.ebi.biostd.model.Section
import uk.ac.ebi.biostd.model.Submission
import uk.ac.ebi.biostd.tsv.deserialization.model.FileChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.FileTableChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.LinkChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.LinksTableChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.SectionChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.SectionTableChunk
import uk.ac.ebi.biostd.tsv.deserialization.model.TsvChunk
import uk.ac.ebi.biostd.validation.DuplicatedSectionAccNoException
import uk.ac.ebi.biostd.validation.SectionNotFoundException
import uk.ac.ebi.biostd.validation.SerializationError
import uk.ac.ebi.biostd.validation.SerializationException

@Suppress("TooManyFunctions")
internal class TsvSerializationContext {
    private val sections = TsvSectionContext()
    private var errors: Multimap<Any, SerializationError> = HashMultimap.create()

    private var submission: Submission = Submission()
    private var rootSection: Section = Section()
    private var currentSection: Section = rootSection

    fun getSubmission() = if (errors.isEmpty) submission else throw SerializationException(submission, errors)

    fun addSubmission(
        chunk: TsvChunk,
        function: (TsvChunk) -> Submission,
    ) = execute(submission, chunk) { submission = function(chunk) }

    fun addRootSection(
        chunk: TsvChunk,
        function: (TsvChunk) -> Section,
    ) {
        execute(submission, chunk) { rootSection = function(chunk) }
        submission.section = addSection(rootSection)
    }

    fun addLink(chunk: LinkChunk) = execute(currentSection, chunk) { currentSection.addLink(chunk.asLink()) }

    fun addFile(chunk: FileChunk) = execute(currentSection, chunk) { currentSection.addFile(chunk.asFile()) }

    fun addLinksTable(chunk: LinksTableChunk) = execute(currentSection, chunk) { currentSection.addLinksTable(chunk.asTable()) }

    fun addFilesTable(chunk: FileTableChunk) = execute(currentSection, chunk) { currentSection.addFilesTable(chunk.asTable()) }

    fun addSectionTable(chunk: SectionTableChunk) = execute(rootSection, chunk) { rootSection.addSectionTable(chunk.asTable()) }

    fun addSubSectionTable(
        parent: String,
        chunk: SectionTableChunk,
    ) = execute(rootSection, chunk) { sections.getSection(parent).addSectionTable(chunk.asTable()) }

    fun addSection(chunk: SectionChunk) = execute(rootSection, chunk) { addSection(rootSection, chunk.asSection()) }

    fun addSubSection(
        parent: String,
        chunk: SectionChunk,
    ) = execute(rootSection, chunk) { addSection(sections.getSection(parent), chunk.asSection()) }

    private fun addSection(
        parent: Section,
        section: Section,
    ) = parent.addSection(addSection(section))

    private fun addSection(section: Section): Section {
        section.accNo?.let { sections.addSection(it, section) }
        currentSection = section
        return currentSection
    }

    @Suppress("TooGenericExceptionCaught")
    private fun <T> execute(
        parent: T,
        chunk: TsvChunk,
        function: (TsvChunk) -> Unit,
    ) {
        try {
            function(chunk)
        } catch (exception: Exception) {
            errors.put(parent, SerializationError(chunk, exception))
        }
    }
}

internal class TsvSectionContext {
    private val sections: MutableMap<String, Section> = mutableMapOf()

    fun addSection(
        accNo: String,
        section: Section,
    ) {
        require(sections.containsKey(accNo).not()) { throw DuplicatedSectionAccNoException(accNo) }
        sections[accNo] = section
    }

    fun getSection(accNo: String) = sections.getOrElse(accNo) { throw SectionNotFoundException(accNo) }
}
