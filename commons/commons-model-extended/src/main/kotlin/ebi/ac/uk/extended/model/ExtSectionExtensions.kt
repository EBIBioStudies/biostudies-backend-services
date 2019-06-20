package ebi.ac.uk.extended.model

val ExtSection.allSections
    get(): List<ExtSection> = sections.flatMap { either -> either.fold({ listOf(it) }, { it.sections }) }

val ExtSection.allReferencedFiles
    get(): List<ExtFile> = libraryFile?.referencedFiles.orEmpty()

val ExtSection.allFiles
    get(): List<ExtFile> = files.flatMap { either -> either.fold({ listOf(it) }, { it.files }) }
