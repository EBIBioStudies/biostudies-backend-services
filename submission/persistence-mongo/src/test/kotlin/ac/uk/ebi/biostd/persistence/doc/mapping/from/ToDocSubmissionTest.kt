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
import ac.uk.ebi.biostd.persistence.doc.model.FireDocDirectory
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.COLLECTION_ACC_NO
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.CREATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_STAT_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.EXT_TAG_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.MODIFICATION_TIME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.NFS_FILENAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.NFS_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.NFS_FULL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.NFS_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.RELEASE_TIME
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
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTRIBUTE_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_LINK_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_REL_PATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILEPATH
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_FILE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_NAME
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_REFERENCE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTRIBUTE_VALUE
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTR_NAME_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_LINK_ATTR_VALUE_ATTRS
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.ROOT_SEC_TABLE_REL_PATH
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
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fireDirectory
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fireFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.fullExtSubmission
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionFileListFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.rootSectionTableFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.subSection
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.subSectionFileListFile
import ac.uk.ebi.biostd.persistence.doc.test.doc.ext.subSectionTable
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.test.createFile
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import io.github.glytching.junit.extension.folder.TemporaryFolder
import io.github.glytching.junit.extension.folder.TemporaryFolderExtension
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(TemporaryFolderExtension::class)
class ToDocSubmissionTest(tempFolder: TemporaryFolder) {
    private val newRootSectionFileListFile =
        rootSectionFileListFile.copy(file = tempFolder.createFile("tempFile1.txt", "content1"))
    private val newSubSectionFileListFile =
        subSectionFileListFile.copy(file = tempFolder.createFile("tempFile2.txt", "content2"))
    private val newRootSectionFile = rootSectionFile.copy(file = tempFolder.createFile("tempFile3.txt", "content3"))
    private val newRootSectionTableFile =
        rootSectionTableFile.copy(file = tempFolder.createFile("tempFile4.txt", "content4"))

    private val newSubSection =
        subSection.copy(fileList = subSection.fileList!!.copy(files = listOf(newSubSectionFileListFile)))

    private val nfsFileFile = tempFolder.createFile(NFS_FILENAME)
    private val nfsFile = NfsFile(NFS_FILENAME, NFS_FILEPATH, NFS_REL_PATH, NFS_FULL_PATH, nfsFileFile)

    private val newRootSection = rootSection.copy(
        fileList = rootSection.fileList!!.copy(
            files = listOf(newRootSectionFileListFile),
            pageTabFiles = listOf(fireFile, fireDirectory, nfsFile)
        ),
        sections = listOf(
            Either.left(newSubSection),
            Either.right(ExtSectionTable(sections = listOf(subSectionTable)))
        ),
        files = listOf(
            Either.left(newRootSectionFile),
            Either.right(ExtFileTable(files = listOf(newRootSectionTableFile)))
        )
    )

    private val submission = fullExtSubmission.copy(
        section = newRootSection,
        pageTabFiles = listOf(fireFile, fireDirectory, nfsFile)
    )

    @Test
    fun `to Doc Submission with a file inside the section and another file inside the inner section`() {
        val (docSubmission, listFiles) = submission.toDocSubmission()

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

        assertPageTabFiles(docSubmission.pageTabFiles)
    }

    private fun assertPageTabFiles(pageTabFiles: List<DocFile>) {
        assertThat(pageTabFiles).hasSize(3)
        assertThat(pageTabFiles.first()).isEqualTo(
            FireDocFile(
                fireFile.fileName,
                fireFile.filePath,
                fireFile.relPath,
                fireFile.fireId,
                listOf(),
                fireFile.md5,
                fireFile.size
            )
        )
        assertThat(pageTabFiles.second()).isEqualTo(
            FireDocDirectory(
                fireDirectory.fileName, fireDirectory.filePath,
                fireDirectory.relPath, listOf(), fireDirectory.md5, fireDirectory.size
            )
        )
        assertThat(pageTabFiles.third()).isEqualTo(
            NfsDocFile(
                nfsFile.fileName,
                nfsFile.filePath,
                nfsFile.relPath,
                nfsFileFile.absolutePath,
                listOf(),
                nfsFileFile.md5(),
                nfsFile.size,
                "file"
            )
        )
    }

    private fun assertListFiles(listFiles: List<FileListDocFile>, docSubmissionId: ObjectId) {
        assertThat(listFiles).hasSize(2)

        val listFile = listFiles[0]
        assertThat(listFile.submissionId).isEqualTo(docSubmissionId)
        assertThat(listFile.fileName).isEqualTo(ROOT_FILE_LIST_FILE_NAME)
        assertThat(listFile.fullPath).isEqualTo(newRootSectionFileListFile.file.path)

        val sublistFile = listFiles[1]
        assertThat(sublistFile.submissionId).isEqualTo(docSubmissionId)
        assertThat(sublistFile.fileName).isEqualTo(SUB_FILE_LIST_FILE_NAME)
        assertThat(sublistFile.fullPath).isEqualTo(newSubSectionFileListFile.file.path)
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
        assertPageTabFiles(docSection.fileList!!.pageTabFiles)
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
        val docFile = docFiles.first()
        assertThat(docFile).hasLeftValueSatisfying {
            require(docFile is Either.Left)
            require(docFile.a is NfsDocFile)
            assertThat((docFile.a as NfsDocFile).fileName).isEqualTo(ROOT_SEC_FILE_NAME)
            assertThat((docFile.a as NfsDocFile).filePath).isEqualTo(ROOT_SEC_FILEPATH)
            assertThat((docFile.a as NfsDocFile).relPath).isEqualTo(ROOT_SEC_REL_PATH)
            assertThat((docFile.a as NfsDocFile).fullPath).isEqualTo(newRootSectionFile.file.path)
        }

        val docFileTable = docFiles.second()
        assertThat(docFileTable).hasRightValueSatisfying {
            val innerNfsDocFile = it.files.first()
            require(innerNfsDocFile is NfsDocFile)
            assertThat(innerNfsDocFile.fileName).isEqualTo(ROOT_SEC_TABLE_FILE_NAME)
            assertThat(innerNfsDocFile.filePath).isEqualTo(ROOT_SEC_TABLE_FILEPATH)
            assertThat(innerNfsDocFile.relPath).isEqualTo(ROOT_SEC_TABLE_REL_PATH)
            assertThat(innerNfsDocFile.fullPath).isEqualTo(newRootSectionTableFile.file.path)
        }
    }
}
