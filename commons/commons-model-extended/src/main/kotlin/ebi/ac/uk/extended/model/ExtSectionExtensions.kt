package ebi.ac.uk.extended.model

import arrow.core.Either

val ExtSection.allSections
    get(): List<ExtSection> =
        sections.flatMap { either -> either.fold({ listOf(it) + it.allSections }, { it.sections }) }

val ExtSection.allFiles
    get(): List<ExtFile> = files.flatMap { either -> either.fold({ listOf(it) }, { it.files }) }

val ExtSection.title
    get(): String? = attributes.find { it.name == "Title" }?.value

/**
 * Replace the section and it subsections by calling the given function over all section file list.
 */
fun ExtSection.replaceFileList(replaceFunction: (file: ExtFileList) -> ExtFileList): ExtSection = copy(
    fileList = fileList?.let { replaceFunction(it) },
    sections = sections.map { processSections(it, replaceFunction) }
)

private fun processSections(
    sections: Either<ExtSection, ExtSectionTable>,
    replaceFunction: (file: ExtFileList) -> ExtFileList
): Either<ExtSection, ExtSectionTable> =
    sections.bimap(
        { it.replaceFileList(replaceFunction) },
        { it.copy(sections = it.sections.map { subSect -> subSect.replaceFileList(replaceFunction) }) }
    )
