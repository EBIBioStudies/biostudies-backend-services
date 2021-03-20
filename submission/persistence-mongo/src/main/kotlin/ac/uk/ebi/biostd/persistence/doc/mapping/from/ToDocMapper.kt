package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocAttributeDetail
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import org.bson.types.ObjectId

// File Mapping
internal fun Either<ExtFile, ExtFileTable>.toDocFiles() = bimap(ExtFile::toDocFile, ExtFileTable::toDocFileTable)
internal fun ExtFileList.toDocFileList(submissionId: ObjectId): Pair<DocFileList, List<FileListDocFile>> {
    val listFiles = files.map { toFileDocListFile(submissionId, it) }
    val listRef = listFiles.map { DocFileRef(fileId = it.id) }

    return Pair(DocFileList(fileName, listRef), listFiles)
}

private fun toFileDocListFile(submissionId: ObjectId, extFile: ExtFile) = FileListDocFile(
    id = ObjectId(),
    submissionId = submissionId,
    fileName = extFile.fileName,
    fullPath = extFile.file.absolutePath,
    attributes = extFile.attributes.map { it.toDocAttribute() },
    md5 = extFile.md5
)

private fun ExtFileTable.toDocFileTable() = DocFileTable(files.map { it.toDocFile() })
private fun ExtFile.toDocFile(): DocFile =
    DocFile(fileName, file.absolutePath, attributes.map { it.toDocAttribute() }, md5)

// Links Mapping
internal fun Either<ExtLink, ExtLinkTable>.toDocLinks() = bimap(ExtLink::toDocLink, ExtLinkTable::toDocLinkTable)

private fun ExtLink.toDocLink(): DocLink = DocLink(url, attributes.map { it.toDocAttribute() })
private fun ExtLinkTable.toDocLinkTable() = DocLinkTable(links.map { it.toDocLink() })

// Attributes Mapping
internal fun ExtAttribute.toDocAttribute() =
    DocAttribute(name, value, reference, nameAttrs.toExtAttr(), valueAttrs.toExtAttr())

private fun List<ExtAttributeDetail>.toExtAttr() = map { DocAttributeDetail(it.name, it.value) }
