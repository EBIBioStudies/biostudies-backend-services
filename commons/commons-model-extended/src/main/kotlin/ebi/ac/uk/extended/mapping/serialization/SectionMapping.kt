package ebi.ac.uk.extended.mapping.serialization

import ebi.ac.uk.extended.integration.FilesSource
import ebi.ac.uk.extended.model.ExtLibraryFile
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.model.LibraryFile
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable

class SectionMapper(
    private val attributeMapper: AttributeMapper,
    private val fileMapper: FileMapping) {

    internal fun toExtSection(section: Section, fileSource: FilesSource): ExtSection = section.run {
        ExtSection(
            type = type,
            accNo = accNo,
            libraryFile = section.libraryFile?.let { getLibraryFile(it, fileSource) },
            attributes = attributeMapper.toAttributes(attributes),
            sections = sections.map {
                it.bimap(
                    { section -> this@SectionMapper.toExtSection(section, fileSource) },
                    { table -> asExtSectionTable(table, fileSource) })
            })
    }


    private fun asExtSectionTable(table: SectionsTable, fileSource: FilesSource): ExtSectionTable =
        ExtSectionTable(table.elements.map { section -> this@SectionMapper.toExtSection(section, fileSource) })

    private fun getLibraryFile(libraryFile: LibraryFile, fileSource: FilesSource): ExtLibraryFile {
        val file = fileSource.get(libraryFile.name)
        val referencedFiles = libraryFile.referencedFiles.map { fileMapper.toExtFile(it, fileSource) }
        return ExtLibraryFile(file = file, fileName = libraryFile.name, referencedFiles = referencedFiles)
    }


}
