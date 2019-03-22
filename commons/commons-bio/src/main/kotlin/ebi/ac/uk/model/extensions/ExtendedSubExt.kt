package ebi.ac.uk.model.extensions

import ebi.ac.uk.model.ExtendedSubmission

fun ExtendedSubmission.allExtendedSections() = extendedSection.allExtendedSections() + extendedSection
fun ExtendedSubmission.allReferencedFiles() = allExtendedSections().flatMap { it.allReferencedFiles() }
fun ExtendedSubmission.allLibraryFileSections() = allExtendedSections().filterNot { it.libraryFile == null }
