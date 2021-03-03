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
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ACC_NO1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME4
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME_ATTRS1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME_ATTRS2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME_ATTRS3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_NAME_ATTRS4
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_REFERENCE1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_REFERENCE2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_REFERENCE3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_REFERENCE4
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE4
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE_ATTRS1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE_ATTRS2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE_ATTRS3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ATTRIBUTE_VALUE_ATTRS4
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_FILE_LIST_FILENAME1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_FILE_LIST_FILENAME2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.OWNER1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.RELEASED
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.RELEASE_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.REL_PATH1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_FILE_LIST_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_FILE_LIST_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_PATH1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SECTION_LINK_URL
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SECTION_TABLE_LINK_URL
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SECRET_KEY1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_ACC_NO1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_ACC_NO2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_ACC_NO3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_TYPE1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_TYPE2
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SEC_TYPE3
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUBMITTER1
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_FILE_LIST_FILE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.SUB_FILE_LIST_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.TITLE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.VERSION
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
        assertAttribute(docSubmission.attributes.first())

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
        assertThat(docSubmission.accNo).isEqualTo(ACC_NO1)
        assertThat(docSubmission.version).isEqualTo(VERSION)
        assertThat(docSubmission.owner).isEqualTo(OWNER1)
        assertThat(docSubmission.submitter).isEqualTo(SUBMITTER1)
        assertThat(docSubmission.title).isEqualTo(TITLE)
        assertThat(docSubmission.method).isEqualTo(DocSubmissionMethod.PAGE_TAB)
        assertThat(docSubmission.relPath).isEqualTo(REL_PATH1)
        assertThat(docSubmission.rootPath).isEqualTo(ROOT_PATH1)
        assertThat(docSubmission.released).isEqualTo(RELEASED)
        assertThat(docSubmission.secretKey).isEqualTo(SECRET_KEY1)
        assertThat(docSubmission.status).isEqualTo(DocProcessingStatus.PROCESSED)
        assertThat(docSubmission.releaseTime).isEqualTo(RELEASE_TIME.toInstant())
        assertThat(docSubmission.modificationTime).isEqualTo(MODIFICATION_TIME.toInstant())
        assertThat(docSubmission.creationTime).isEqualTo(CREATION_TIME.toInstant())
    }

    private fun assertRootSection(docSection: DocSection) {
        assertThat(docSection.accNo).isEqualTo(SEC_ACC_NO1)
        assertThat(docSection.type).isEqualTo(SEC_TYPE1)
        assertThat(docSection.fileList?.fileName).isEqualTo(EXT_FILE_LIST_FILENAME1)

        assertAttribute1(docSection.attributes.first())
        assertInnerSections(docSection.sections)
        assertFiles(docSection.files)
        assertLinks(docSection.links)
    }

    private fun assertAttribute1(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME1)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE1)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE1)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS1)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS1)
    }

    private fun assertAttribute2(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME2)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE2)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE2)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS2)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS2)
    }

    private fun assertAttribute3(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME3)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE3)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE3)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS3)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS3)
    }

    private fun assertAttribute4(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME4)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE4)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE4)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS4)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS4)
    }

    private fun assertAttribute(docAttribute: DocAttribute) {
        assertThat(docAttribute.name).isEqualTo(ATTRIBUTE_NAME1)
        assertThat(docAttribute.value).isEqualTo(ATTRIBUTE_VALUE1)
        assertThat(docAttribute.reference).isEqualTo(ATTRIBUTE_REFERENCE1)
        assertThat(docAttribute.nameAttrs).isEqualTo(ATTRIBUTE_NAME_ATTRS1)
        assertThat(docAttribute.valueAttrs).isEqualTo(ATTRIBUTE_VALUE_ATTRS1)
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
            assertThat(it.accNo).isEqualTo(SEC_ACC_NO2)
            assertThat(it.type).isEqualTo(SEC_TYPE2)
            assertThat(it.fileList?.fileName).isEqualTo(EXT_FILE_LIST_FILENAME2)
        }

        assertThat(docSections.second()).hasRightValueSatisfying {
            assertThat(it.sections.first().accNo).isEqualTo(SEC_ACC_NO3)
            assertThat(it.sections.first().type).isEqualTo(SEC_TYPE3)
            assertAttribute2(it.sections.first().attributes.first())
        }
    }

    private fun assertLinks(docLinks: List<Either<DocLink, DocLinkTable>>) {
        assertThat(docLinks.first()).hasLeftValueSatisfying {
            assertThat(it.url).isEqualTo(ROOT_SECTION_LINK_URL)
            assertAttribute3(it.attributes.first())
        }
        assertThat(docLinks.first()).hasRightValueSatisfying {
            assertThat(it.links.first().url).isEqualTo(ROOT_SECTION_TABLE_LINK_URL)
            assertAttribute4(it.links.first().attributes.first())
        }
    }

    private fun assertFiles(docFiles: List<Either<DocFile, DocFileTable>>) {
        assertThat(docFiles.first()).hasLeftValueSatisfying {
            assertThat(it.relPath).isEqualTo(ROOT_SEC_FILE_NAME)
            assertThat(it.fullPath).isEqualTo(ROOT_SEC_FILE.absolutePath)
        }
        assertThat(docFiles.second()).hasRightValueSatisfying {
            assertThat(it.files.first().relPath).isEqualTo(ROOT_SEC_TABLE_FILE)
            assertThat(it.files.first().fullPath).isEqualTo(ROOT_SEC_TABLE_FILE_NAME.absolutePath)
        }
    }
}
