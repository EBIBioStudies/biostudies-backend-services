package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import org.bson.types.ObjectId

class ToDocSubmissionMapper(
    private val toDocSectionMapper: ToDocSectionMapper,
) {
    fun convert(sub: ExtSubmission): DocSubmissionData {
        val subId = ObjectId()
        val (docSection, fileList, linkList) = toDocSectionMapper.convert(sub.section, sub.accNo, sub.version, subId)
        val docSubmission = sub.convert(subId, docSection)
        return DocSubmissionData(docSubmission, fileList, linkList)
    }

    private fun ExtSubmission.convert(
        submissionId: ObjectId,
        docSection: DocSection,
    ): DocSubmission =
        DocSubmission(
            id = submissionId,
            accNo = accNo,
            title = title,
            doi = doi,
            method = getMethod(method),
            version = version,
            schemaVersion = schemaVersion,
            relPath = relPath,
            rootPath = rootPath,
            released = released,
            secretKey = secretKey,
            creationTime = creationTime.toInstant(),
            modificationTime = modificationTime.toInstant(),
            releaseTime = releaseTime?.toInstant(),
            owner = owner,
            submitter = submitter,
            collections = collections.map { DocCollection(it.accNo) },
            tags = tags.map { DocTag(it.name, it.value) },
            attributes = attributes.map { it.toDocAttribute() },
            section = docSection,
            pageTabFiles = pageTabFiles.map { it.toDocFile() },
            storageMode = storageMode,
        )

    private fun getMethod(method: ExtSubmissionMethod) =
        when (method) {
            ExtSubmissionMethod.FILE -> DocSubmissionMethod.FILE
            ExtSubmissionMethod.PAGE_TAB -> DocSubmissionMethod.PAGE_TAB
            ExtSubmissionMethod.UNKNOWN -> DocSubmissionMethod.UNKNOWN
        }
}
