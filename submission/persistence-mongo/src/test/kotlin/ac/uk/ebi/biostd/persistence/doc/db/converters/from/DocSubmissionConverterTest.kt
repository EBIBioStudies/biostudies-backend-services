package ac.uk.ebi.biostd.persistence.doc.db.converters.from

import ac.uk.ebi.biostd.persistence.doc.db.converters.shared.DocSubmissionFields
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.docSubmissionClass
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.Document
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Date

@ExtendWith(MockKExtension::class)
internal class DocSubmissionConverterTest(
    @MockK val docAttributeConverter: DocAttributeConverter,
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
        assertThat(result).isInstanceOf(DocSubmission::class.java)
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
        assertThat(result.collections[0].accNo).isEqualTo(projectDocAccNo)
        assertThat(result.stats[0].name).isEqualTo(statDocName)
        assertThat(result.stats[0].value).isEqualTo(statDocValue)
    }

    private fun createSubmissionDocument(): Document {
        val subDocument = Document()
        subDocument[DocSubmissionFields.CLASS_FIELD] = docSubmissionClass
        subDocument[DocSubmissionFields.SUB_ID] = subId
        subDocument[DocSubmissionFields.SUB_ACC_NO] = subAccNo
        subDocument[DocSubmissionFields.SUB_VERSION] = subVersion
        subDocument[DocSubmissionFields.SUB_OWNER] = subOwner
        subDocument[DocSubmissionFields.SUB_SUBMITTER] = subSubmitter
        subDocument[DocSubmissionFields.SUB_TITLE] = subTitle
        subDocument[DocSubmissionFields.SUB_METHOD] = subMethod
        subDocument[DocSubmissionFields.SUB_REL_PATH] = subRelPath
        subDocument[DocSubmissionFields.SUB_ROOT_PATH] = subRootPath
        subDocument[DocSubmissionFields.SUB_RELEASED] = subReleased
        subDocument[DocSubmissionFields.SUB_SECRET_KEY] = subSecretKey
        subDocument[DocSubmissionFields.SUB_STATUS] = subStatus
        subDocument[DocSubmissionFields.SUB_RELEASE_TIME] = subReleaseTime
        subDocument[DocSubmissionFields.SUB_MODIFICATION_TIME] = subModificationTime
        subDocument[DocSubmissionFields.SUB_CREATION_TIME] = subCreationTime
        subDocument[DocSubmissionFields.SUB_SECTION] = sectionDocument
        subDocument[DocSubmissionFields.SUB_ATTRIBUTES] = listOf(AttributeDocument)
        subDocument[DocSubmissionFields.SUB_TAGS] = listOf(createTagDocument())
        subDocument[DocSubmissionFields.SUB_PROJECTS] = listOf(createProjectDocument())
        subDocument[DocSubmissionFields.SUB_STATS] = listOf(createStatDocument())
        return subDocument
    }

    private fun createTagDocument(): Document {
        val tagDoc = Document()
        tagDoc[DocSubmissionFields.TAG_DOC_NAME] = tagDocName
        tagDoc[DocSubmissionFields.TAG_DOC_VALUE] = tagDocValue
        return tagDoc
    }

    private fun createProjectDocument(): Document {
        val projectDoc = Document()
        projectDoc[DocSubmissionFields.PROJECT_DOC_ACC_NO] = projectDocAccNo
        return projectDoc
    }

    private fun createStatDocument(): Document {
        val statDoc = Document()
        statDoc[DocSubmissionFields.STAT_DOC_NAME] = statDocName
        statDoc[DocSubmissionFields.STAT_DOC_VALUE] = statDocValue
        return statDoc
    }

    companion object {
        val subId = ObjectId(1, 1)
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
