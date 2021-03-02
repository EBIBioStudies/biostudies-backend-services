package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.test.fullExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.EXT_FILE_FILENAME
import ac.uk.ebi.biostd.persistence.doc.test.EXT_FILE_FILE
import ac.uk.ebi.biostd.persistence.doc.test.SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.test.ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.VERSION
import ac.uk.ebi.biostd.persistence.doc.test.OWNER1
import ac.uk.ebi.biostd.persistence.doc.test.SUBMITTER1
import ac.uk.ebi.biostd.persistence.doc.test.TITLE
import ac.uk.ebi.biostd.persistence.doc.test.METHOD
import ac.uk.ebi.biostd.persistence.doc.test.REL_PATH1
import ac.uk.ebi.biostd.persistence.doc.test.ROOT_PATH1
import ac.uk.ebi.biostd.persistence.doc.test.RELEASED
import ac.uk.ebi.biostd.persistence.doc.test.STATUS
import ac.uk.ebi.biostd.persistence.doc.test.RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.test.MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.EXT_FILE_LIST_FILENAME1
import ac.uk.ebi.biostd.persistence.doc.test.ATTRIBUTE_NAME1
import ac.uk.ebi.biostd.persistence.doc.test.ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.ATTRIBUTE_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.ATTRIBUTE_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.EXT_TAG_NAME
import ac.uk.ebi.biostd.persistence.doc.test.EXT_TAG_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.EXT_FILE_LIST_FILENAME2
import ac.uk.ebi.biostd.persistence.doc.test.EXT_LINK_URL
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ToDocSubmissionTest {

    @Test
    fun `to Doc Submission with a file inside the section and another file inside the inner section`() {
        val (docSubmission, listFiles) = fullExtSubmission.toDocSubmission()

        assertDocSubmission(docSubmission)
        assertListFiles(listFiles, docSubmission)
    }

    private fun assertDocSubmission(docSubmission: DocSubmission) {
        assertSimpleDocProperties(docSubmission)
        assertSection(docSubmission.section)

        assertThat(docSubmission.attributes).hasSize(1)
        assertAttribute(docSubmission.attributes[0])

        assertThat(docSubmission.tags).hasSize(1)
        assertTag(docSubmission.tags[0])
        assertThat(docSubmission.collections).hasSize(1)

        assertCollection(docSubmission.collections[0])
    }

    private fun assertListFiles(listFiles: List<FileListDocFile>, docSubmission: DocSubmission) {
        assertThat(listFiles).hasSize(2)

        val listFile = listFiles[0]
        assertThat(listFile.id).isEqualTo(docSubmission.section.fileList?.files?.get(0)?.fileId)
        assertThat(listFile.submissionId).isEqualTo(docSubmission.id)
        assertThat(listFile.fileName).isEqualTo(EXT_FILE_FILENAME)
        assertThat(listFile.fullPath).isEqualTo(EXT_FILE_FILE.absolutePath)

        assertThat(docSubmission.section.sections[0]).hasLeftValueSatisfying {
            val sublistFile = listFiles[1]
            assertThat(sublistFile.id).isEqualTo(it.fileList?.files?.get(0)?.fileId)
            assertThat(sublistFile.submissionId).isEqualTo(docSubmission.id)
            assertThat(sublistFile.fileName).isEqualTo(EXT_FILE_FILENAME)
            assertThat(sublistFile.fullPath).isEqualTo(EXT_FILE_FILE.absolutePath)
        }

        val docSection = docSubmission.section.sections[0] as Either.Left<DocSection>
        assertThat(listFiles[1].id).isEqualTo(docSection.a.fileList?.files?.get(0)?.fileId)
        assertThat(listFiles[1].submissionId).isEqualTo(docSubmission.id)
        assertThat(listFiles[1].fileName).isEqualTo(EXT_FILE_FILENAME)
        assertThat(listFiles[1].fullPath).isEqualTo(EXT_FILE_FILE.absolutePath)
    }

    private fun assertSimpleDocProperties(docSubmission: DocSubmission) {
        assertThat(docSubmission.accNo).isEqualTo(ACC_NO)
        assertThat(docSubmission.version).isEqualTo(VERSION)
        assertThat(docSubmission.owner).isEqualTo(OWNER1)
        assertThat(docSubmission.submitter).isEqualTo(SUBMITTER1)
        assertThat(docSubmission.title).isEqualTo(TITLE)
        assertThat(docSubmission.method).isEqualTo(getMethod(METHOD))
        assertThat(docSubmission.relPath).isEqualTo(REL_PATH1)
        assertThat(docSubmission.rootPath).isEqualTo(ROOT_PATH1)
        assertThat(docSubmission.released).isEqualTo(RELEASED)
        assertThat(docSubmission.secretKey).isEqualTo(SECRET_KEY)
        assertThat(docSubmission.status).isEqualTo(getStatus(STATUS))
        assertThat(docSubmission.releaseTime).isEqualTo(RELEASE_TIME.toInstant())
        assertThat(docSubmission.modificationTime).isEqualTo(MODIFICATION_TIME.toInstant())
        assertThat(docSubmission.creationTime).isEqualTo(CREATION_TIME.toInstant())
    }

    private fun assertSection(docSection: DocSection) {
        assertThat(docSection.accNo).isEqualTo(SEC_ACC_NO)
        assertThat(docSection.type).isEqualTo(SEC_TYPE)
        assertThat(docSection.fileList?.fileName).isEqualTo(EXT_FILE_LIST_FILENAME1)

        assertAttribute(docSection.attributes[0])
        assertInnerSections(docSection.sections)
        assertFiles(docSection.files)
        assertLinks(docSection.links)
    }

    private fun assertAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME1)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS)
    }

    private fun assertCollection(docCollection: DocCollection) {
        assertThat(docCollection.accNo).isEqualTo(COLLECTION_ACC_NO)
    }

    private fun assertTag(docTag: DocTag) {
        assertThat(docTag.name).isEqualTo(EXT_TAG_NAME)
        assertThat(docTag.value).isEqualTo(EXT_TAG_VALUE)
    }

    private fun assertInnerSections(docSections: List<Either<DocSection, DocSectionTable>>) {
        val docSection = docSections[0] as Either.Left<DocSection>
        assertThat(docSection.a.type).isEqualTo(SEC_TYPE)
        assertThat(docSection.a.fileList?.fileName).isEqualTo(EXT_FILE_LIST_FILENAME2)

        val docSectionTable = docSections[1] as Either.Right<DocSectionTable>
        assertThat(docSectionTable.b.sections[0].type).isEqualTo(SEC_TYPE)
        // assertThat(docSectionTable.b.sections[0].fileList).isNull()
    }

    private fun assertLinks(docLinks: List<Either<DocLink, DocLinkTable>>) {

        val docLink = docLinks[0] as Either.Left<DocLink>
        assertThat(docLink.a.url).isEqualTo(EXT_LINK_URL)

        val docLinkTable = docLinks[1] as Either.Right<DocLinkTable>
        assertThat(docLinkTable.b.links[0].url).isEqualTo(EXT_LINK_URL)
    }

    private fun assertFiles(docFiles: List<Either<DocFile, DocFileTable>>) {
        val docFile = docFiles[0] as Either.Left<DocFile>
        assertFile(docFile.a)

        val docFileTable = docFiles[1] as Either.Right<DocFileTable>
        assertFile(docFileTable.b.files[0])
    }

    private fun assertFile(docFile: DocFile) {
        assertThat(docFile.relPath).isEqualTo(EXT_FILE_FILENAME)
        assertThat(docFile.fullPath).isEqualTo(EXT_FILE_FILE.absolutePath)
    }

    private fun getMethod(method: ExtSubmissionMethod) =
        when (method) {
            ExtSubmissionMethod.FILE -> DocSubmissionMethod.FILE
            ExtSubmissionMethod.PAGE_TAB -> DocSubmissionMethod.PAGE_TAB
            ExtSubmissionMethod.UNKNOWN -> DocSubmissionMethod.UNKNOWN
        }

    private fun getStatus(status: ExtProcessingStatus) =
        when (status) {
            ExtProcessingStatus.PROCESSED -> DocProcessingStatus.PROCESSED
            ExtProcessingStatus.PROCESSING -> DocProcessingStatus.PROCESSING
            ExtProcessingStatus.REQUESTED -> DocProcessingStatus.REQUESTED
        }
}
