package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.to.SubmissionConverter
import ac.uk.ebi.biostd.persistence.doc.model.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
internal class DocSubmissionConverterTest(@MockK val docAttributeConverter: DocAttributeConverter,
                                          @MockK val AttributeDocument: Document,
                                          @MockK val docAttribute: DocAttribute,

                                          @MockK val docSectionConverter: DocSectionConverter,
                                          @MockK val sectionDocument: Document,
                                          @MockK val docSection: DocSection
) {
    private val testInstance = DocSubmissionConverter(docSectionConverter, docAttributeConverter)

    @Test
    fun convert() {
        every { docSectionConverter.convert(sectionDocument) } returns docSection
        every { docAttributeConverter.convert(AttributeDocument) } returns docAttribute

        val result = testInstance.convert(createSubmissionDocument())

        assertThatAll(result)
    }

    private fun assertThatAll(result: DocSubmission) {
        assertThat(result).isInstanceOf(docSubClazz)
        assertThat(result.id).isEqualTo(subId)
        assertThat(result.accNo).isEqualTo(subAccNo)
        assertThat(result.version).isEqualTo(subVersion)
        assertThat(result.owner).isEqualTo(subOwner)
        assertThat(result.submitter).isEqualTo(subSubmitter)
        assertThat(result.title).isEqualTo(subTitle)
        assertThat(result.method).isEqualTo(DocSubmissionMethod.fromString(subMethod))
        assertThat(result.relPath).isEqualTo(subRelPath)
        assertThat(result.rootPath).isEqualTo(subRootPath)
        assertThat(result.released).isEqualTo(subReleased)
        assertThat(result.secretKey).isEqualTo(subSecretKey)
        assertThat(result.status).isEqualTo(DocProcessingStatus.fromString(subStatus))
        assertThat(result.releaseTime).isEqualTo(subReleaseTime.toInstant())
        assertThat(result.modificationTime).isEqualTo(subModificationTime.toInstant())
        assertThat(result.creationTime).isEqualTo(subCreationTime.toInstant())
        assertThat(result.section).isEqualTo(docSection)
        assertThat(result.attributes).isEqualTo(listOf(docAttribute))
        assertThat(result.tags[0].name).isEqualTo(tagDocName)
        assertThat(result.tags[0].value).isEqualTo(tagDocValue)
        assertThat(result.projects[0].accNo).isEqualTo(projectDocAccNo)
        assertThat(result.stats[0].name).isEqualTo(statDocName)
        assertThat(result.stats[0].value).isEqualTo(statDocValue)
    }

    private fun createSubmissionDocument(): Document {
        val subDocument = Document()
        subDocument[SubmissionConverter.subClass] = docSubmissionClass
        subDocument[DocSubmissionConverter.subId] = subId
        subDocument[DocSubmissionConverter.subAccNo] = subAccNo
        subDocument[DocSubmissionConverter.subVersion] = subVersion
        subDocument[DocSubmissionConverter.subOwner] = subOwner
        subDocument[DocSubmissionConverter.subSubmitter] = subSubmitter
        subDocument[DocSubmissionConverter.subTitle] = subTitle
        subDocument[DocSubmissionConverter.subMethod] = subMethod
        subDocument[DocSubmissionConverter.subRelPath] = subRelPath
        subDocument[DocSubmissionConverter.subRootPath] = subRootPath
        subDocument[DocSubmissionConverter.subReleased] = subReleased
        subDocument[DocSubmissionConverter.subSecretKey] = subSecretKey
        subDocument[DocSubmissionConverter.subStatus] = subStatus
        subDocument[DocSubmissionConverter.subReleaseTime] = subReleaseTime
        subDocument[DocSubmissionConverter.subModificationTime] = subModificationTime
        subDocument[DocSubmissionConverter.subCreationTime] = subCreationTime
        subDocument[DocSubmissionConverter.subSection] = sectionDocument
        subDocument[DocSubmissionConverter.subAttributes] = listOf(AttributeDocument)
        subDocument[DocSubmissionConverter.subTags] = listOf(createTagDocument())
        subDocument[DocSubmissionConverter.subProjects] = listOf(createProjectDocument())
        subDocument[DocSubmissionConverter.subStats] = listOf(createStatDocument())
        return subDocument
    }

    private fun createTagDocument(): Document {
        val tagDoc = Document()
        tagDoc[SubmissionConverter.tagDocName] = tagDocName
        tagDoc[SubmissionConverter.tagDocValue] = tagDocValue
        return tagDoc
    }

    private fun createProjectDocument(): Document {
        val projectDoc = Document()
        projectDoc[SubmissionConverter.projectDocAccNo] = projectDocAccNo
        return projectDoc
    }

    private fun createStatDocument(): Document {
        val statDoc = Document()
        statDoc[SubmissionConverter.statDocName] = statDocName
        statDoc[SubmissionConverter.statDocValue] = statDocValue
        return statDoc
    }

    companion object {
        val docSubClazz = DocSubmission::class.java
        const val subId = "id"
        const val subAccNo = "accNo"
        const val projectDocAccNo = "accNo"
        const val subVersion = 1
        const val subOwner = "owner"
        const val subSubmitter = "submitter"
        const val subTitle = "title"
        const val subMethod = "FILE"
        const val subStatus = "PROCESSED"
        const val subRelPath = "relPath"
        const val subRootPath = "rootPath"
        const val subReleased = false
        const val subSecretKey = "secretKey"
        val subReleaseTime: Date = Date(110)
        val subModificationTime: Date = Date(220)
        val subCreationTime: Date = Date(330)
        const val tagDocName = "name"
        const val tagDocValue = "value"
        const val statDocName = "name"
        const val statDocValue: Long = 1
    }
}
