package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.Submission

fun ExtendedSubmission.allExtendedSections() = extendedSection.allExtendedSections() + extendedSection
fun ExtendedSubmission.allReferencedFiles() = allExtendedSections().flatMap { it.allReferencedFiles() }
fun ExtendedSubmission.allLibraryFileSections() = allExtendedSections().filterNot { it.libraryFile == null }
fun ExtendedSubmission.asSubmission() = Submission(accNo, extendedSection.asSection(), attributes)
