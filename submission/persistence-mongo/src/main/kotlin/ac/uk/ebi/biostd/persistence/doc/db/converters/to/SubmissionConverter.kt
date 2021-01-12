package ac.uk.ebi.biostd.persistence.doc.db.converters.to

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.CommonsConverter.classField
import ac.uk.ebi.biostd.persistence.doc.model.DocProject
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import org.bson.Document
import org.springframework.core.convert.converter.Converter

class SubmissionConverter(
    private val sectionConverter: SectionConverter,
    private val attributeConverter: AttributeConverter
) : Converter<DocSubmission, Document> {

    override fun convert(submission: DocSubmission): Document {
        val submissionDoc = Document()
        submissionDoc[classField] = clazz
        submissionDoc[subId] = submission.id
        submissionDoc[subAccNo] = submission.accNo
        submissionDoc[subVersion] = submission.version
        submissionDoc[subOwner] = submission.owner
        submissionDoc[subSubmitter] = submission.submitter
        submissionDoc[subTitle] = submission.title
        submissionDoc[subMethod] = submission.method.value
        submissionDoc[subRelPath] = submission.relPath
        submissionDoc[subRootPath] = submission.rootPath
        submissionDoc[subReleased] = submission.released
        submissionDoc[subSecretKey] = submission.secretKey
        submissionDoc[subStatus] = submission.status.value
        submissionDoc[subReleaseTime] = submission.releaseTime
        submissionDoc[subModificationTime] = submission.modificationTime
        submissionDoc[subCreationTime] = submission.creationTime
        submissionDoc[subSection] = sectionConverter.convert(submission.section)
        submissionDoc[subAttributes] = submission.attributes.map { attributeConverter.convert(it) }
        submissionDoc[subTags] = submission.tags.map { tagToDocument(it) }
        submissionDoc[subProjects] = submission.projects.map { projectToDocument(it) }
        submissionDoc[subStats] = submission.stats.map { statToDocument(it) }
        return submissionDoc
    }

    companion object {
        val clazz: String = DocSubmission::class.java.canonicalName
        val docTagClassName: String = DocSubmission::class.java.canonicalName
        val docProjectClassName: String = DocSubmission::class.java.canonicalName
        val docStatClassName: String = DocSubmission::class.java.canonicalName
        const val subId = "id"
        const val subClass = "_class"
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

    private fun tagToDocument(docTag: DocTag): Document {
        val tagDoc = Document()
        tagDoc[classField] = docTagClassName
        tagDoc[tagDocName] = docTag.name
        tagDoc[tagDocValue] = docTag.value
        return tagDoc
    }
    private fun projectToDocument(docProject: DocProject): Document {
        val projectDoc = Document()
        projectDoc[classField] = docProjectClassName
        projectDoc[projectDocAccNo] = docProject.accNo
        return projectDoc
    }
    private fun statToDocument(docStat: DocStat): Document {
        val statDoc = Document()
        statDoc[classField] = docStatClassName
        statDoc[statDocName] = docStat.name
        statDoc[statDocValue] = docStat.value
        return statDoc
    }
}
