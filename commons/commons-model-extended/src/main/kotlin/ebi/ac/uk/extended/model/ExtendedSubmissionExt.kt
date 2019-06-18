package ebi.ac.uk.extended.model

fun ExtSubmission.allSections(): List<ExtSection> = section.allExtendedSections() + section
fun ExtSubmission.allReferencedFiles() = allSections().flatMap { it.allReferencedFiles() }
fun ExtSubmission.allFiles() = allSections().flatMap { it.allFiles() }
fun ExtSubmission.allLibraryFiles(): List<ExtLibraryFile> =
    allSections().filterNot { it.libraryFile == null }.map { it.libraryFile!! }

fun ExtSection.allExtendedSections(): List<ExtSection> =
    sections.flatMap { either -> either.fold({ listOf(it) }, { it.sections }) }

fun ExtSection.allReferencedFiles(): List<ExtFile> = libraryFile?.referencedFiles.orEmpty()

fun ExtSection.allFiles() = files.flatMap { either -> either.fold({ listOf(it) }, { it.files }) }
