package ac.uk.ebi.biostd.persistence.doc.db.converters.shared

import ac.uk.ebi.biostd.persistence.doc.model.DocAttribute
import ac.uk.ebi.biostd.persistence.doc.model.DocCollection
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileTable
import ac.uk.ebi.biostd.persistence.doc.model.DocLink
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkList
import ac.uk.ebi.biostd.persistence.doc.model.DocLinkTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSection
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTable
import ac.uk.ebi.biostd.persistence.doc.model.DocSectionTableRow
import ac.uk.ebi.biostd.persistence.doc.model.DocSubmission
import ac.uk.ebi.biostd.persistence.doc.model.DocTag
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile

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
    const val FILE_DOC_FILENAME = "fileName"
    const val FILE_DOC_FILEPATH = "filePath"
    const val FILE_DOC_REL_PATH = "relPath"
    const val FILE_DOC_ATTRIBUTES = "attributes"
    const val FILE_DOC_MD5 = "md5"
    const val FILE_DOC_SIZE = "fileSize"
    const val FILE_DOC_TYPE = "type"
}

object NfsDocFileFields {
    val NFS_DOC_FILE_CLASS: String = NfsDocFile::class.java.canonicalName
    const val NFS_FILE_FULL_PATH = "fullPath"
}

object FireDocFileFields {
    val FIRE_DOC_FILE_CLASS: String = FireDocFile::class.java.canonicalName
    const val FIRE_FILE_DOC_ID = "fireId"
}

object DocFileListFields {
    val DOC_FILE_LIST_CLASS: String = DocFileList::class.java.canonicalName
    const val FILE_LIST_DOC_FILE_FILENAME = "fileName"
    const val FILE_LIST_DOC_PAGE_TAB_FILES = "pageTabFiles"
}

object DocLinkListFields {
    val DOC_LINK_LIST_CLASS: String = DocLinkList::class.java.canonicalName
    const val LINK_LIST_DOC_FILE_FILENAME = "fileName"
    const val LINK_LIST_DOC_PAGE_TAB_FILES = "pageTabFiles"
}

object FileListDocFileFields {
    const val FILE_LIST_DOC_FILE_ID = "_id"
    const val FILE_LIST_DOC_FILE_SUBMISSION_ID = "submissionId"
    const val FILE_LIST_DOC_FILE_SUBMISSION_ACC_NO = "submissionAccNo"
    const val FILE_LIST_DOC_FILE_SUBMISSION_VERSION = "submissionVersion"
    const val FILE_LIST_DOC_FILE_FILE = "file"
    const val FILE_LIST_DOC_FILE_INDEX = "index"
    const val FILE_LIST_DOC_FILE_FILE_LIST_NAME = "fileListName"
}

object DocSubmissionRequestFileFields {
    const val RQT_FILE_INDEX = "index"
    const val RQT_FILE_FILE = "file"
    const val RQT_FILE_STATUS = "status"
    const val RQT_FILE_PATH = "path"
    const val RQT_PREVIOUS_SUB_FILE = "previousSubFile"
    const val RQT_FILE_SUB_ACC_NO = "accNo"
    const val RQT_FILE_SUB_VERSION = "version"
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
    const val SEC_LINK_LIST = "linkList"
    const val SEC_SECTIONS = "sections"
    const val SEC_FILES = "files"
    const val SEC_LINKS = "links"
    const val SEC_ATTRIBUTES = "attributes"
    const val SEC_TABLE_SECTIONS = "sections"
}

object DocSubmissionFields {
    val DOC_SUBMISSION_CLASS: String = DocSubmission::class.java.canonicalName
    val DOC_TAG_CLASS: String = DocTag::class.java.canonicalName
    val DOC_COLLECTION_CLASS: String = DocCollection::class.java.canonicalName

    const val CLASS_FIELD = "_class"
    const val SUB = "submission"
    const val SUB_ID = "id"
    const val SUB_ACC_NO = "accNo"
    const val SUB_VERSION = "version"
    const val SUB_SCHEMA_VERSION = "schemaVersion"
    const val SUB_OWNER = "owner"
    const val SUB_SUBMITTER = "submitter"
    const val SUB_TITLE = "title"
    const val SUB_DOI = "doi"
    const val SUB_METHOD = "method"
    const val SUB_REL_PATH = "relPath"
    const val SUB_ROOT_PATH = "rootPath"
    const val SUB_RELEASED = "released"
    const val SUB_SECRET_KEY = "secretKey"
    const val SUB_RELEASE_TIME = "releaseTime"
    const val SUB_MODIFICATION_TIME = "modificationTime"
    const val SUB_CREATION_TIME = "creationTime"
    const val SUB_SECTION = "section"
    const val SUB_ATTRIBUTES = "attributes"
    const val SUB_TAGS = "tags"
    const val SUB_COLLECTIONS = "collections"
    const val TAG_DOC_NAME = "name"
    const val TAG_DOC_VALUE = "value"
    const val COLLECTION_ACC_NO = "accNo"
    const val PAGE_TAB_FILES = "pageTabFiles"
    const val STORAGE_MODE = "storageMode"
}

object DocRequestFields {
    const val RQT_ACC_NO = "accNo"
    const val RQT_VERSION = "version"
    const val RQT_OWNER = "owner"
    const val RQT_DRAFT = "draft"
    const val RQT_STATUS = "status"
    const val RQT_ERRORS = "errors"
    const val RQT_MODIFICATION_TIME = "modificationTime"
    const val RQT_PROCESS = "process"
    const val RQT_TOTAL_FILES = "totalFiles"
    const val RQT_FILE_CHANGES = "fileChanges"
    const val RQT_IDX = "currentIndex"
    const val RQT_PREV_SUB_VERSION = "previousVersion"
    const val RQT_STATUS_CHANGES = "statusChanges"
    const val RQT_STATUS_CHANGE_STATUS_ID = "statusId"
    const val RQT_STATUS_CHANGE_END_TIME = "endTime"
    const val RQT_STATUS_CHANGE_RESULT = "result"
    const val RQT_PREFERED_SOURCES = "preferredSources"
    const val RQT_FILES = "files"
    const val RQT_ON_BEHALF = "onBehalfUser"
}

object DocStatsFields {
    const val STATS_ACC_NO = "accNo"
    const val STATS_STATS_MAP = "stats"
    const val STATS_LAST_UPDATED = "lastUpdated"
    const val STATS_FILE_SIZE = "FILES_SIZE"
    const val STATS_DIRECTORIES = "DIRECTORIES"
    const val STATS_NON_DECLARED_FILES_DIRECTORIES = "NON_DECLARED_FILES_DIRECTORIES"
}
