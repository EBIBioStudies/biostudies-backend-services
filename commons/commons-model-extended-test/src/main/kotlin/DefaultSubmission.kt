import DefaultAttribute.Companion.defaultAttribute
import DefaultCollection.Companion.defaultCollection
import DefaultSection.Companion.defaultSection
import DefaultStat.Companion.defaultStat
import DefaultTag.Companion.defaultTag
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
import java.io.File
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

class DefaultSubmission private constructor() {
    companion object {
        @Suppress("LongParameterList")
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
            pageTabFiles = pageTabFiles
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
        val ATTRIBUTES = listOf(defaultAttribute())
        val TAGS = listOf(defaultTag())
        val COLLECTIONS = listOf(defaultCollection())
        val STATS = listOf(defaultStat())
        val SECTION = defaultSection()
        val PAGE_TAG_FILES = emptyList<ExtFile>()
    }
}

class DefaultTag {
    private constructor()

    companion object {
        fun defaultTag(name: String = NAME, value: String = VALUE) = ExtTag(name = name, value = value)
        const val NAME = "name"
        const val VALUE = "value"
    }
}

class DefaultCollection {
    private constructor()

    companion object {
        fun defaultCollection(accNo: String = ACC_NO) = ExtCollection(accNo = accNo)
        const val ACC_NO = "value"
    }
}

class DefaultAttributeDetail {
    private constructor()

    companion object {
        fun defaultAttributeDetail(
            name: String = NAME,
            value: String = VALUE
        ) = ExtAttributeDetail(
            name = name,
            value = value
        )

        const val NAME = "name"
        const val VALUE = "value"
    }
}

class DefaultLink {
    private constructor()

    companion object {
        fun defaultLink(
            url: String = URL,
            attributes: List<ExtAttribute> = ATTRIBUTES
        ) = ExtLink(
            url = url,
            attributes = attributes
        )

        const val URL = "url"
        val ATTRIBUTES = listOf(defaultAttribute())
    }
}

sealed class DefaultFile

class DefaultFireFile : DefaultFile {
    private constructor()

    companion object {
        @Suppress("LongParameterList")
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

        const val FILE_PATH = "filePath"
        const val REL_PATH = "relPath"
        const val FIRE_ID = "fireId"
        const val MD5 = "md5"
        const val SIZE = 1L
        val ATTRIBUTES = emptyList<ExtAttribute>()
    }
}

class DefaultFireDirectory : DefaultFile {
    private constructor()

    companion object {
        fun defaultFireDirectory(
            filePath: String = FILE_PATH,
            relPath: String = REL_PATH,
            md5: String = MD5,
            size: Long = SIZE,
            attributes: List<ExtAttribute> = ATTRIBUTES,
        ) = FireDirectory(
            filePath = filePath,
            relPath = relPath,
            md5 = md5,
            size = size,
            attributes = attributes
        )

        const val FILE_PATH = "filePath"
        const val REL_PATH = "relPath"
        const val MD5 = "md5"
        const val SIZE = 1L
        val ATTRIBUTES = emptyList<ExtAttribute>()
    }
}

class DefaultNfsFile : DefaultFile {
    private constructor()

    companion object {
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
            attributes = attributes
        )

        const val FILE_PATH = "filePath"
        const val FULL_PATH = "fullPath"
        const val REL_PATH = "relPath"
        val FILE = File("")
        val ATTRIBUTES = emptyList<ExtAttribute>()
    }
}

class DefaultFileList {
    private constructor()

    companion object {
        fun defaultFileList(
            fileName: String = FILE_NAME,
            files: List<ExtFile> = FILES,
            filesUrl: String? = FILES_URL,
            pageTabFiles: List<ExtFile> = PAGE_TAG_FILES,
        ) = ExtFileList(
            fileName = fileName,
            files = files,
            filesUrl = filesUrl,
            pageTabFiles = pageTabFiles
        )

        const val FILE_NAME = "fileName"
        val FILES = emptyList<ExtFile>()
        const val FILES_URL = "filesUrl"
        val PAGE_TAG_FILES = emptyList<ExtFile>()
    }
}

class DefaultSectionTable {
    private constructor()

    companion object {
        fun defaultSectionTable(
            sections: List<ExtSection> = SECTIONS
        ) = ExtSectionTable(
            sections = sections
        )

        val SECTIONS = listOf(defaultSection())
    }
}

class DefaultLinkTable {
    private constructor()

    companion object {
        fun defaultLinkTable(
            links: List<ExtLink> = LINKS
        ) = ExtLinkTable(
            links = links
        )

        val LINKS = listOf(DefaultLink.defaultLink())
    }
}

class DefaultFileTable {
    private constructor()

    companion object {
        fun defaultFileTable(
            files: List<ExtFile> = FILES
        ) = ExtFileTable(
            files = files
        )

        val FILES = listOf(
            DefaultFireFile.defaultFireFile(),
            DefaultFireDirectory.defaultFireDirectory(),
            DefaultNfsFile.defaultNfsFile()
        )
    }
}

class DefaultAttribute {
    private constructor()

    companion object {
        fun defaultAttribute(
            name: String = NAME,
            value: String = VALUE,
            reference: Boolean = REFERENCE,
            nameAttrs: List<ExtAttributeDetail> = NAME_ATTRS,
            valueAttrs: List<ExtAttributeDetail> = VALUE_ATTRS,
        ) = ExtAttribute(
            name = name,
            value = value,
            reference = reference,
            nameAttrs = nameAttrs,
            valueAttrs = valueAttrs
        )

        const val NAME = "name"
        const val VALUE = "value"
        const val REFERENCE = false
        val NAME_ATTRS = listOf(DefaultAttributeDetail.defaultAttributeDetail())
        val VALUE_ATTRS = listOf(DefaultAttributeDetail.defaultAttributeDetail())
    }
}

class DefaultSection {
    private constructor()

    companion object {
        @Suppress("LongParameterList")
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
}

class DefaultStat {
    private constructor()

    companion object {
        fun defaultStat(name: String = NAME, value: String = VALUE) = ExtStat(name = name, value = value)
        const val NAME = "name"
        const val VALUE = "value"
    }
}
