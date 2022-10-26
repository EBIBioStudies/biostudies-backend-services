package ebi.ac.uk.extended.model

val ExtSection.allSections
    get(): List<ExtSection> =
        sections.flatMap { either -> either.fold({ listOf(it) + it.allSections }, { it.sections }) }

val ExtSection.allInnerFiles
    get(): List<ExtFile> = files.flatMap { either -> either.fold({ listOf(it) }, { it.files }) }

val ExtSection.title
    get(): String? = attributes.find { it.name == "Title" }?.value
