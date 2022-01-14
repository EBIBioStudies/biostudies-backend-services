package ebi.ac.uk.extended.mapping.from

import ac.uk.ebi.biostd.integration.SerializationService
import ac.uk.ebi.biostd.integration.SubFormat
import ac.uk.ebi.biostd.integration.SubFormat.TsvFormat.XlsxTsv
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.io.sources.FilesSource
import ebi.ac.uk.io.sources.FireBioFile
import ebi.ac.uk.io.sources.FireDirectoryBioFile
import ebi.ac.uk.io.sources.NfsBioFile
import ebi.ac.uk.model.FileList
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.constants.RESERVED_ATTRIBUTES
import ebi.ac.uk.util.file.ExcelReader
import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream

private val logger = KotlinLogging.logger {}

class SectionMapper(private val fileListMapper: FileListMapper) {

    fun toExtSection(sec: Section, source: FilesSource): ExtSection = ExtSection(
        type = sec.type,
        accNo = sec.accNo,
        fileList = sec.fileList?.let { fileListMapper.toExtFileList(it, source) },
        attributes = sec.attributes.filterNot { RESERVED_ATTRIBUTES.contains(it.name) }.map { it.toExtAttribute() },
        files = sec.files.map { either -> either.bimap({ it.toExtFile(source) }, { it.toExtTable(source) }) },
        links = sec.links.map { either -> either.bimap({ it.toExtLink() }, { it.toExtTable() }) },
        sections = sec.sections.map { either -> either.bimap({ toExtSection(it, source) }, { toExtTable(it, source) }) }
    )

    private fun toExtTable(table: SectionsTable, fileSource: FilesSource): ExtSectionTable {
        return ExtSectionTable(table.elements.map { toExtSection(it, fileSource) })
    }
}

class FileListMapper(private val pageTabSerializer: SerializationService) {
    internal fun toExtFileList(fileList: FileList, fileSource: FilesSource): ExtFileList {
        var fileListFile = getFile(fileList.name, fileSource)
        var subFormat = SubFormat.fromFile(fileListFile)
        if (subFormat == XlsxTsv) {
            fileListFile = ExcelReader.asTsv(fileListFile)
            subFormat = SubFormat.TsvFormat.Tsv
        }
        val files = fileListFile.inputStream().use { toExtFiles(fileSource, it, subFormat) }
        return ExtFileList(fileList.name.substringBeforeLast("."), files)
    }

    private fun getFile(fileList: String, source: FilesSource): File {
        return when (val bioFile = source.getFile(fileList)) {
            is FireBioFile -> TODO()
            is FireDirectoryBioFile -> TODO()
            is NfsBioFile -> bioFile.file
        }
    }

    private fun toExtFiles(fileSource: FilesSource, stream: FileInputStream, format: SubFormat): List<ExtFile> {
        return pageTabSerializer.deserializeFileList(stream, format)
            .onEach { file -> logger.info { "mapping file ${file.path}" } }
            .map { it.toExtFile(fileSource) }
            .toList()
    }
}
