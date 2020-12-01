package ac.uk.ebi.biostd.persistence.doc.mapping.to

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.io.sources.FilesSource

internal fun DocSection.toExtSection(source: FilesSource): ExtSection {
    return ExtSection(
        accNo = accNo,
        type = type,
        fileList = fileList?.toExtFileList(source),
        attributes = attributes.map { it.toExtAttribute() },
        sections = emptyList(),
        files = files.map { it.toExtFiles(source) },
        links = emptyList())
    //  sections = sections.toExtSections(filesSource),
    //  files = files.toExtFiles(filesSource),
    //  links = links.toExtLinks())
}

// File Mapping
private fun Either<DocFile, DocFileTable>.toExtFiles(source: FilesSource) = bimap({ it.toExtFile(source) }) { it.toExtFileTable(source) }
private fun DocFile.toExtFile(source: FilesSource) = ExtFile(filePath, source.getFile(filePath), attributes.map { it.toExtAttribute() })
private fun DocFileTable.toExtFileTable(source: FilesSource) = ExtFileTable(files.map { it.toExtFile(source) })
private fun DocFileList.toExtFileList(source: FilesSource) = ExtFileList(fileName, files.map { it.toExtFile(source) })

private fun DocAttribute.toExtAttribute() = ExtAttribute(name, value, reference, nameAttrs.map { it.toExtDetail() }, valueAttrs.map { it.toExtDetail() })
private fun DocAttributeDetail.toExtDetail(): ExtAttributeDetail = ExtAttributeDetail(name, value)

