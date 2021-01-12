package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import org.bson.Document
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class DocSubmissionConverter(
    private val docSectionConverter: DocSectionConverter,
    private val docAttributeConverter: DocAttributeConverter
) : Converter<Document, DocSubmission> {
    override fun convert(source: Document): DocSubmission {
        return DocSubmission(
            id = source.getString(subId),
            accNo = source.getString(subAccNo),
            version = source.getInteger(subVersion),
            owner = source.getString(subOwner),
            submitter = source.getString(subSubmitter),
            title = source.getString(subTitle),
            method = DocSubmissionMethod.fromString(source.getString(subMethod)),
            relPath = source.getString(subRelPath),
            rootPath = source.getString(subRootPath),
            released = source.getBoolean(subReleased),
            secretKey = source.getString(subSecretKey),
            status = DocProcessingStatus.fromString(source.getString(subStatus)),
            releaseTime = source.getDate(subReleaseTime).toInstant(),
            modificationTime = source.getDate(subModificationTime).toInstant(),
            creationTime = source.getDate(subCreationTime).toInstant(),
            section = docSectionConverter.convert(source.getDoc(subSection)),
            attributes = source.getDocList(subAttributes).map { docAttributeConverter.convert(it) },
            tags = source.getDocList(subTags).map { toDocTag(it) },
            projects = source.getDocList(subProjects).map { toDocProject(it) },
            stats = source.getDocList(subStats).map { toDocStat(it) }
        )
    }

    private fun toDocTag(doc: Document): DocTag =
        DocTag(name = doc.getString(tagDocName), value = doc.getString(tagDocValue))

    private fun toDocStat(doc: Document): DocStat =
        DocStat(name = doc.getString(statDocName), value = doc.getLong(statDocValue))

    private fun toDocProject(doc: Document): DocProject = DocProject(accNo = doc.getString(projectDocAccNo))

    companion object {
        const val subId = "id"
        const val subAccNo = "accNo"
        const val subVersion = "version"
        const val subOwner = "owner"
        const val subSubmitter = "submitter"
        const val subTitle = "title"
        const val subMethod = "method"
        const val subRelPath = "relPath"
        const val subRootPath = "rootPath"
        const val subReleased = "released"
        const val subSecretKey = "secretKey"
        const val subStatus = "status"
        const val subReleaseTime = "releaseTime"
        const val subModificationTime = "modificationTime"
        const val subCreationTime = "creationTime"
        const val subSection = "section"
        const val subAttributes = "attributes"
        const val subTags = "tags"
        const val subProjects = "projects"
        const val tagDocName = "name"
        const val tagDocValue = "value"
        const val projectDocAccNo = "accNo"
        const val statDocName = "name"
        const val statDocValue = "value"
        const val subStats = "stats"
    }
}
