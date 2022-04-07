@file:Suppress("LongParameterList", "MagicNumber")
package uk.ac.ebi.extended.test

import uk.ac.ebi.extended.test.SectionFactory.defaultSection
import arrow.core.Either
import ebi.ac.uk.extended.model.ExtAttribute
import ebi.ac.uk.extended.model.ExtAttributeDetail
import ebi.ac.uk.extended.model.ExtCollection
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtProcessingStatus
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import ebi.ac.uk.extended.model.ExtStat
import ebi.ac.uk.extended.model.ExtSubmission
import ebi.ac.uk.extended.model.ExtSubmissionMethod
import ebi.ac.uk.extended.model.ExtTag
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.extended.model.StorageMode
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

object SubmissionFactory {
    fun defaultSubmission(
        accNo: String = ACC_NO,
        version: Int = VERSION,
        schemaVersion: String = SCHEMA_VERSION,
        owner: String = OWNER,
        submitter: String = SUBMITTER,
        title: String? = TITLE,
        method: ExtSubmissionMethod = METHOD,
        relPath: String = REL_PATH,
        rootPath: String? = ROOT_PATH,
        released: Boolean = RELEASED,
        secretKey: String = SECRET_KEY,
        status: ExtProcessingStatus = STATUS,
        releaseTime: OffsetDateTime? = RELEASE_TIME,
        modificationTime: OffsetDateTime = MODIFICATION_TIME,
        creationTime: OffsetDateTime = CREATION_TIME,
        section: ExtSection = SECTION,
        attributes: List<ExtAttribute> = ATTRIBUTES,
        tags: List<ExtTag> = TAGS,
        collections: List<ExtCollection> = COLLECTIONS,
        stats: List<ExtStat> = STATS,
        pageTabFiles: List<ExtFile> = PAGE_TAG_FILES
    ) = ExtSubmission(
        accNo = accNo,
        version = version,
        schemaVersion = schemaVersion,
        owner = owner,
        submitter = submitter,
        title = title,
        method = method,
        relPath = relPath,
        rootPath = rootPath,
        released = released,
        secretKey = secretKey,
        status = status,
        releaseTime = releaseTime,
        modificationTime = modificationTime,
        creationTime = creationTime,
        section = section,
        attributes = attributes,
        tags = tags,
        collections = collections,
        stats = stats,
        pageTabFiles = pageTabFiles,
        storageMode = StorageMode.NFS
    )

    const val ACC_NO = "S-TEST123"
    const val VERSION = 1
    const val SCHEMA_VERSION = "1.0"
    const val OWNER = "owner@email.org"
    const val SUBMITTER = "submitter@email.org"
    const val TITLE = "Default Submission Title"
    val METHOD = ExtSubmissionMethod.PAGE_TAB
    const val REL_PATH = "S-TEST/123/S-TEST123"
    const val ROOT_PATH = "SUBMISSION_ROOT_PATH"
    const val RELEASED = false
    const val SECRET_KEY = "SUBMISSION_SECRET_KEY"
    val STATUS = ExtProcessingStatus.PROCESSED
    val RELEASE_TIME: OffsetDateTime = OffsetDateTime.of(2019, 9, 21, 0, 0, 0, 0, UTC)
    val MODIFICATION_TIME: OffsetDateTime = OffsetDateTime.of(2020, 9, 21, 0, 0, 0, 0, UTC)
    val CREATION_TIME: OffsetDateTime = OffsetDateTime.of(2018, 9, 21, 0, 0, 0, 0, UTC)
    val ATTRIBUTES = emptyList<ExtAttribute>()
    val TAGS = emptyList<ExtTag>()
    val COLLECTIONS = emptyList<ExtCollection>()
    val STATS = emptyList<ExtStat>()
    val SECTION = defaultSection()
    val PAGE_TAG_FILES = emptyList<ExtFile>()
}

object SectionFactory {
    fun defaultSection(
        accNo: String? = ACC_NO,
        type: String = TYPE,
        fileList: ExtFileList? = FILE_LIST,
        attributes: List<ExtAttribute> = ATTRIBUTES,
        sections: List<Either<ExtSection, ExtSectionTable>> = SECTIONS,
        files: List<Either<ExtFile, ExtFileTable>> = FILES,
        links: List<Either<ExtLink, ExtLinkTable>> = LINKS
    ) = ExtSection(
        accNo = accNo,
        type = type,
        fileList = fileList,
        attributes = attributes,
        sections = sections,
        files = files,
        links = links,
    )

    const val ACC_NO = "accNo"
    const val TYPE = "Study"
    val FILE_LIST = null
    val ATTRIBUTES = emptyList<ExtAttribute>()
    val SECTIONS = emptyList<Either<ExtSection, ExtSectionTable>>()
    val FILES = emptyList<Either<ExtFile, ExtFileTable>>()
    val LINKS = emptyList<Either<ExtLink, ExtLinkTable>>()
}

object FireFileFactory {
    fun defaultFireFile(
        filePath: String = FILE_PATH,
        relPath: String = REL_PATH,
        fireId: String = FIRE_ID,
        md5: String = MD5,
        size: Long = SIZE,
        attributes: List<ExtAttribute> = ATTRIBUTES,
    ) = FireFile(
        filePath = filePath,
        relPath = relPath,
        fireId = fireId,
        md5 = md5,
        size = size,
        attributes = attributes
    )

    const val FILE_PATH = "folder/file.txt"
    const val REL_PATH = "Files/folder/file.txt"
    const val FIRE_ID = "fireId"
    const val MD5 = "md5"
    const val SIZE = 1L
    val ATTRIBUTES = emptyList<ExtAttribute>()
}

object FireDirectoryFactory {
    fun defaultFireDirectory(
        filePath: String = FILE_PATH,
        relPath: String = REL_PATH,
        fireId: String = FIRE_ID,
        md5: String = MD5,
        size: Long = SIZE,
        attributes: List<ExtAttribute> = ATTRIBUTES,
    ) = FireDirectory(
        filePath = filePath,
        relPath = relPath,
        fireId = fireId,
        md5 = md5,
        size = size,
        attributes = attributes
    )

    const val FIRE_ID = "dirFireId"
    const val FILE_PATH = "folder/file.txt"
    const val REL_PATH = "Files/folder/file.txt"
    const val MD5 = "md5"
    const val SIZE = 1L
    val ATTRIBUTES = emptyList<ExtAttribute>()
}

object NfsFileFactory {
    fun defaultNfsFile(
        filePath: String = FILE_PATH,
        relPath: String = REL_PATH,
        fullPath: String = FULL_PATH,
        file: File = FILE,
        attributes: List<ExtAttribute> = ATTRIBUTES,
    ) = NfsFile(
        filePath = filePath,
        relPath = relPath,
        fullPath = fullPath,
        file = file,
        md5 = file.md5(),
        size = file.size(),
        attributes = attributes
    )

    const val FILE_PATH = "folder/file.txt"
    const val FULL_PATH = "root/Files/folder/file.txt"
    const val REL_PATH = "Files/folder/file.txt"
    val FILE = File("")
    val ATTRIBUTES = emptyList<ExtAttribute>()
}

object FileListFactory {
    fun defaultFileList(
        filePath: String = FILE_PATH,
        files: List<ExtFile> = FILES,
        filesUrl: String? = FILES_URL,
        pageTabFiles: List<ExtFile> = PAGE_TAG_FILES,
    ) = ExtFileList(
        filePath = filePath,
        files = files,
        filesUrl = filesUrl,
        pageTabFiles = pageTabFiles
    )

    const val FILE_PATH = "folder/fileList.txt"
    val FILES = emptyList<ExtFile>()
    const val FILES_URL = "filesUrl"
    val PAGE_TAG_FILES = emptyList<ExtFile>()
}

object AttributeFactory {
    fun defaultAttribute(
        name: String = NAME,
        value: String = VALUE,
        reference: Boolean = REFERENCE,
        nameAttrs: List<ExtAttributeDetail> = listOf(),
        valueAttrs: List<ExtAttributeDetail> = listOf()

    ) = ExtAttribute(name, value, reference, nameAttrs, valueAttrs)

    const val NAME = "name"
    const val VALUE = "value"
    const val REFERENCE = false
}
