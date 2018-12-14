package ac.uk.ebi.biostd.tsv.deserialization

import ac.uk.ebi.biostd.validation.SerializationError
import ac.uk.ebi.biostd.validation.SerializationException
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import ebi.ac.uk.model.File
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Submission

class TsvSerializationContext {

    private val sections: MutableMap<String, Section> = mutableMapOf()
    private var errors: Multimap<Any, SerializationError> = HashMultimap.create()

    private var submission: Submission = Submission()
    private var rootSection: Section = Section()
    private var currentSection: Section = rootSection

    fun getSubmission() = if (errors.isEmpty) submission else throw SerializationException(submission, errors)

    fun addSubmission(function: () -> Submission) {
        execute(submission) { submission = function() }
    }

    fun addRootSection(function: () -> Section) {
        execute(submission) { rootSection = function() }
        submission.section = rootSection
        currentSection = rootSection
    }

    fun addLink(function: () -> Link) = execute(currentSection) { currentSection.addLink(function()) }

    fun addFile(function: () -> File) = execute(currentSection) { currentSection.addFile(function()) }

    fun addLinksTable(function: () -> LinksTable) =
        execute(currentSection) { currentSection.addLinksTable(function()) }

    fun addFilesTable(function: () -> FilesTable) =
        execute(currentSection) { currentSection.addFilesTable(function()) }

    fun addSectionTable(function: () -> SectionsTable) =
        execute(rootSection) { rootSection.addSectionTable(function()) }

    fun addSubSectionTable(parent: String, function: () -> SectionsTable) =
        execute(rootSection) { sections.getValue(parent).addSectionTable(function()) }

    fun addSection(function: () -> Section) =
        execute(rootSection) { addSection(rootSection, function()) }

    fun addSubSection(parent: String, function: () -> Section) =
        execute(rootSection) { addSection(sections.getValue(parent), function()) }

    private fun addSection(parent: Section, section: Section) {
        section.accNo?.let { sections[it] = section }
        parent.addSection(section)
        currentSection = section
    }

    private fun <T> execute(parent: T, function: () -> Unit) {
        try {
            function()
        } catch (exception: Exception) {
            errors.put(parent, SerializationError(exception))
        }
    }
}
