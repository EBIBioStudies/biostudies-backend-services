package ac.uk.ebi.biostd.persistence.doc.mapping.from

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocProcessingStatus
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmissionMethod
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_FILE_LIST_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_FILE_LIST_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SECTION_LINK_URL
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SECTION_TABLE_LINK_URL
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_ATTR_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_EXT_FILE_LIST_FILENAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTR_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ATTR_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_OWNER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_RELEASED
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_ROOT_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_SECRET_KEY
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_SUBMITTER
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_TITLE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMISSION_VERSION
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_FILE_LIST_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_FILE_LIST_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_EXT_FILE_LIST_FILENAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ACC_NO3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ATTR_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ATTR_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ATTR_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_ATTR_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TABLE_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_SEC_TYPE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.util.collections.second
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test

class ToDocSubmissionTest {

    @Test
    fun `to Doc Submission with a file inside the section and another file inside the inner section`() {
        val (docSubmission, listFiles) = fullExtSubmission.toDocSubmission()

        assertDocSubmission(docSubmission)
        assertListFiles(listFiles, docSubmission.id)
        assertFileReferences(docSubmission, listFiles)
    }

    private fun assertFileReferences(docSubmission: DocSubmission, listFiles: List<FileListDocFile>) {
        assertThat(listFiles.first().id).isEqualTo(docSubmission.section.fileList?.files?.first()?.fileId)
        assertThat(docSubmission.section.sections.first()).hasLeftValueSatisfying {
            assertThat(listFiles.second().id).isEqualTo(it.fileList?.files?.first()?.fileId)
        }
    }

    private fun assertDocSubmission(docSubmission: DocSubmission) {
        assertSimpleDocProperties(docSubmission)
        assertRootSection(docSubmission.section)

        assertThat(docSubmission.attributes).hasSize(1)
        assertSubmissionAttribute(docSubmission.attributes.first())

        assertThat(docSubmission.tags).hasSize(1)
        assertTag(docSubmission.tags.first())

        assertThat(docSubmission.collections).hasSize(1)
        assertCollection(docSubmission.collections.first())

        assertThat(docSubmission.stats).hasSize(1)
        assertStat(docSubmission.stats.first())
    }

    private fun assertListFiles(listFiles: List<FileListDocFile>, docSubmissionId: ObjectId) {
        assertThat(listFiles).hasSize(2)

        val listFile = listFiles[0]
        assertThat(listFile.submissionId).isEqualTo(docSubmissionId)
        assertThat(listFile.fileName).isEqualTo(ROOT_FILE_LIST_FILE_NAME)
        assertThat(listFile.fullPath).isEqualTo(ROOT_FILE_LIST_FILE.absolutePath)

        val sublistFile = listFiles[1]
        assertThat(sublistFile.submissionId).isEqualTo(docSubmissionId)
        assertThat(sublistFile.fileName).isEqualTo(SUB_FILE_LIST_FILE_NAME)
        assertThat(sublistFile.fullPath).isEqualTo(SUB_FILE_LIST_FILE.absolutePath)
    }

    private fun assertSimpleDocProperties(docSubmission: DocSubmission) {
        assertThat(docSubmission.accNo).isEqualTo(SUBMISSION_ACC_NO)
        assertThat(docSubmission.version).isEqualTo(SUBMISSION_VERSION)
        assertThat(docSubmission.owner).isEqualTo(SUBMISSION_OWNER)
        assertThat(docSubmission.submitter).isEqualTo(SUBMISSION_SUBMITTER)
        assertThat(docSubmission.title).isEqualTo(SUBMISSION_TITLE)
        assertThat(docSubmission.method).isEqualTo(DocSubmissionMethod.PAGE_TAB)
        assertThat(docSubmission.relPath).isEqualTo(SUBMISSION_REL_PATH)
        assertThat(docSubmission.rootPath).isEqualTo(SUBMISSION_ROOT_PATH)
        assertThat(docSubmission.released).isEqualTo(SUBMISSION_RELEASED)
        assertThat(docSubmission.secretKey).isEqualTo(SUBMISSION_SECRET_KEY)
        assertThat(docSubmission.status).isEqualTo(DocProcessingStatus.PROCESSED)
        assertThat(docSubmission.releaseTime).isEqualTo(RELEASE_TIME.toInstant())
        assertThat(docSubmission.modificationTime).isEqualTo(MODIFICATION_TIME.toInstant())
        assertThat(docSubmission.creationTime).isEqualTo(CREATION_TIME.toInstant())
    }

    private fun assertRootSection(docSection: DocSection) {
        assertThat(docSection.accNo).isEqualTo(ROOT_SEC_ACC_NO)
        assertThat(docSection.type).isEqualTo(ROOT_SEC_TYPE)
        assertThat(docSection.fileList?.fileName).isEqualTo(ROOT_SEC_EXT_FILE_LIST_FILENAME)

        assertRootSectionAttribute(docSection.attributes.first())
        assertInnerSections(docSection.sections)
        assertFiles(docSection.files)
        assertLinks(docSection.links)
    }

    private fun assertRootSectionAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ROOT_SEC_ATTRIBUTE_NAME)
        assertThat(docAttribute.value).isEqualTo(ROOT_SEC_ATTRIBUTE_VALUE)
        assertThat(docAttribute.reference).isEqualTo(ROOT_SEC_ATTRIBUTE_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(ROOT_SEC_ATTR_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(ROOT_SEC_ATTR_VALUE_ATTRS)
    }

    private fun assertSubSectionTableAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(SUB_SEC_TABLE_ATTR_NAME)
        assertThat(docAttribute.value).isEqualTo(SUB_SEC_TABLE_ATTR_VALUE)
        assertThat(docAttribute.reference).isEqualTo(SUB_SEC_TABLE_ATTR_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(SUB_SEC_TABLE_ATTR_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(SUB_SEC_TABLE_ATTR_VALUE_ATTRS)
    }

    private fun assertRootSectionLinkAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ROOT_SEC_LINK_ATTRIBUTE_NAME)
        assertThat(docAttribute.value).isEqualTo(ROOT_SEC_LINK_ATTRIBUTE_VALUE)
        assertThat(docAttribute.reference).isEqualTo(ROOT_SEC_LINK_ATTRIBUTE_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(ROOT_SEC_LINK_ATTR_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(ROOT_SEC_LINK_ATTRIBUTE_VALUE_ATTRS)
    }

    private fun assertRootSectionTableLinkAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ROOT_SEC_TABLE_LINK_ATTRIBUTE_NAME)
        assertThat(docAttribute.value).isEqualTo(ROOT_SEC_TABLE_LINK_ATTRIBUTE_VALUE)
        assertThat(docAttribute.reference).isEqualTo(ROOT_SEC_TABLE_LINK_ATTRIBUTE_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(ROOT_SEC_TABLE_LINK_ATTR_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(ROOT_SEC_TABLE_LINK_ATTR_VALUE_ATTRS)
    }

    private fun assertSubmissionAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(SUBMISSION_ATTRIBUTE_NAME)
        assertThat(docAttribute.value).isEqualTo(SUBMISSION_ATTRIBUTE_VALUE)
        assertThat(docAttribute.reference).isEqualTo(SUBMISSION_ATTRIBUTE_REFERENCE)
        assertThat(docAttribute.nameAttrs).isEqualTo(SUBMISSION_ATTR_NAME_ATTRS)
        assertThat(docAttribute.valueAttrs).isEqualTo(SUBMISSION_ATTR_VALUE_ATTRS)
    }

    private fun assertCollection(docCollection: DocCollection) =
        assertThat(docCollection.accNo).isEqualTo(COLLECTION_ACC_NO)

    private fun assertTag(docTag: DocTag) {
        assertThat(docTag.name).isEqualTo(EXT_TAG_NAME)
        assertThat(docTag.value).isEqualTo(EXT_TAG_VALUE)
    }

    private fun assertStat(docStat: DocStat) {
        assertThat(docStat.name).isEqualTo(EXT_STAT_NAME)
        assertThat(docStat.value).isEqualTo(EXT_STAT_VALUE.toLong())
    }

    private fun assertInnerSections(docSections: List<Either<DocSection, DocSectionTable>>) {
        assertThat(docSections).hasSize(2)

        assertThat(docSections.first()).hasLeftValueSatisfying {
            assertThat(it.accNo).isEqualTo(SUB_SEC_ACC_NO)
            assertThat(it.type).isEqualTo(SUB_SEC_TYPE)
            assertThat(it.fileList?.fileName).isEqualTo(SUB_SEC_EXT_FILE_LIST_FILENAME)
        }

        assertThat(docSections.second()).hasRightValueSatisfying {
            assertThat(it.sections.first().accNo).isEqualTo(SUB_SEC_TABLE_ACC_NO3)
            assertThat(it.sections.first().type).isEqualTo(SUB_SEC_TABLE_TYPE)
            assertSubSectionTableAttribute(it.sections.first().attributes.first())
        }
    }

    private fun assertLinks(docLinks: List<Either<DocLink, DocLinkTable>>) {
        assertThat(docLinks.first()).hasLeftValueSatisfying {
            assertThat(it.url).isEqualTo(ROOT_SECTION_LINK_URL)
            assertRootSectionLinkAttribute(it.attributes.first())
        }
        assertThat(docLinks.first()).hasRightValueSatisfying {
            assertThat(it.links.first().url).isEqualTo(ROOT_SECTION_TABLE_LINK_URL)
            assertRootSectionTableLinkAttribute(it.links.first().attributes.first())
        }
    }

    private fun assertFiles(docFiles: List<Either<DocFile, DocFileTable>>) {
        assertThat(docFiles.first()).hasLeftValueSatisfying {
            assertThat(it.relPath).isEqualTo(ROOT_SEC_FILE_NAME)
            assertThat(it.fullPath).isEqualTo(ROOT_SEC_FILE.absolutePath)
        }
        assertThat(docFiles.second()).hasRightValueSatisfying {
            assertThat(it.files.first().relPath).isEqualTo(ROOT_SEC_TABLE_FILE_NAME)
            assertThat(it.files.first().fullPath).isEqualTo(ROOT_SEC_TABLE_FILE.absolutePath)
        }
    }
}