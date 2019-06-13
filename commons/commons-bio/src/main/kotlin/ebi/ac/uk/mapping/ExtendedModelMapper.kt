package ebi.ac.uk.mapping

import ebi.ac.uk.model.ExtendedSection
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.User

fun toExtendedSection(section: Section): ExtendedSection {
    return ExtendedSection(section.type).apply {
        accNo = section.accNo
        files = section.files
        links = section.links
        sections = section.sections
        attributes = section.attributes
        section.libraryFile?.let { libraryFile = it }
        extendedSections = section.sections.mapTo(mutableListOf()) { either -> either.mapLeft { toExtendedSection(it) } }
    }
}

fun toExtendedSubmission(submission: Submission, user: User): ExtendedSubmission {
    return ExtendedSubmission(submission.accNo, user).apply {
        section = submission.section
        attributes = submission.attributes
        accessTags = submission.accessTags
        extendedSection = toExtendedSection(submission.section)
    }
}
