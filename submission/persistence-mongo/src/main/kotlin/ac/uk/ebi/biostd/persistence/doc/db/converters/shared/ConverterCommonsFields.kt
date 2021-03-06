package ac.uk.ebi.biostd.persistence.doc.db.converters.shared

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ac.uk.ebi.biostd.persistence.doc.model.DocStat
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FileListDocFile

object DocAttributeFields {
    val DOC_ATTRIBUTE_CLASS: String = DocAttribute::class.java.canonicalName
    const val ATTRIBUTE_DOC_NAME = "name"
    const val ATTRIBUTE_DOC_VALUE = "value"
    const val ATTRIBUTE_DOC_NAME_ATTRS = "nameAttrs"
    const val ATTRIBUTE_DOC_REFERENCE = "reference"
    const val ATTRIBUTE_DOC_VALUE_ATTRS = "valueAttrs"
    const val ATTRIBUTE_DETAIL_NAME = "name"
    const val ATTRIBUTE_DETAIL_VALUE = "value"
}

object DocFileFields {
    val DOC_FILE_CLASS: String = DocFile::class.java.canonicalName
    const val FILE_DOC_REL_PATH = "relPath"
    const val FILE_DOC_FULL_PATH = "fullPath"
    const val FILE_DOC_ATTRIBUTES = "attributes"
    const val FILE_DOC_MD5 = "md5"
    const val FILE_TYPE = "type"
    const val FILE_SIZE = "size"
}

object DocFileListFields {
    val DOC_FILE_LIST_CLASS: String = DocFileList::class.java.canonicalName
    const val FILE_LIST_DOC_FILE_FILENAME = "fileName"
    const val FILE_LIST_DOC_FILES = "files"
}

object DocFileRefFields {
    val DOC_FILE_REF_CLASS: String = DocFileRef::class.java.canonicalName
    const val FILE_REF_DOC_FILE_ID = "fileId"
}

object FileListDocFileFields {
    val FILE_LIST_DOC_FILE_CLASS: String = FileListDocFile::class.java.canonicalName
    const val FILE_LIST_DOC_FILE_SUBMISSION_ID = "submissionId"
    const val FILE_LIST_DOC_FILE_FILENAME = "fileName"
    const val FILE_LIST_DOC_FILE_REL_PATH = "relPath"
    const val FILE_LIST_DOC_FILE_FULL_PATH = "fullPath"
    const val FILE_LIST_DOC_FILE_ATTRIBUTES = "attributes"
    const val FILE_LIST_DOC_FILE_MD5 = "md5"
}

object DocFileTableFields {
    val DOC_FILE_TABLE_CLASS: String = DocFileTable::class.java.canonicalName
    const val FILE_TABLE_DOC_FILES = "files"
}

object DocLinkFields {
    val DOC_LINK_CLASS: String = DocLink::class.java.canonicalName
    const val LINK_DOC_URL = "url"
    const val LINK_DOC_ATTRIBUTES = "attributes"
}

object DocLinkTableFields {
    val DOC_LINK_TABLE_CLASS: String = DocLinkTable::class.java.canonicalName
    const val LINK_TABLE_DOC_LINKS = "links"
}

object DocSectionFields {
    val DOC_TABLE_SEC_CLASS: String = DocSectionTableRow::class.java.canonicalName
    val DOC_SEC_CLASS: String = DocSection::class.java.canonicalName
    val DOC_SEC_TABLE_CLASS: String = DocSectionTable::class.java.canonicalName

    const val CLASS_FIELD = "_class"
    const val SEC_ID = "id"
    const val SEC_ACC_NO = "accNo"
    const val SEC_TYPE = "type"
    const val SEC_FILE_LIST = "fileList"
    const val SEC_SECTIONS = "sections"
    const val SEC_FILES = "files"
    const val SEC_LINKS = "links"
    const val SEC_ATTRIBUTES = "attributes"
    const val SEC_TABLE_SECTIONS = "sections"
}

object DocSubmissionFields {
    val DOC_SUBMISSION_CLASS: String = DocSubmission::class.java.canonicalName
    val DOC_TAG_CLASS: String = DocTag::class.java.canonicalName
    val DOC_PROJECT_CLASS: String = DocCollection::class.java.canonicalName
    val DOC_STAT_CLASS: String = DocStat::class.java.canonicalName

    const val CLASS_FIELD = "_class"
    const val SUB_ID = "id"
    const val SUB_ACC_NO = "accNo"
    const val SUB_VERSION = "version"
    const val SUB_OWNER = "owner"
    const val SUB_SUBMITTER = "submitter"
    const val SUB_TITLE = "title"
    const val SUB_METHOD = "method"
    const val SUB_REL_PATH = "relPath"
    const val SUB_ROOT_PATH = "rootPath"
    const val SUB_RELEASED = "released"
    const val SUB_SECRET_KEY = "secretKey"
    const val SUB_STATUS = "status"
    const val SUB_RELEASE_TIME = "releaseTime"
    const val SUB_MODIFICATION_TIME = "modificationTime"
    const val SUB_CREATION_TIME = "creationTime"
    const val SUB_SECTION = "section"
    const val SUB_ATTRIBUTES = "attributes"
    const val SUB_TAGS = "tags"
    const val SUB_PROJECTS = "collections"
    const val TAG_DOC_NAME = "name"
    const val TAG_DOC_VALUE = "value"
    const val PROJECT_DOC_ACC_NO = "accNo"
    const val STAT_DOC_NAME = "name"
    const val STAT_DOC_VALUE = "value"
    const val SUB_STATS = "stats"
}
