@file:Suppress("LongParameterList")

package uk.ac.ebi.extended.test

import uk.ac.ebi.biostd.base.Either
import uk.ac.ebi.biostd.extended.model.ExtAttribute
import uk.ac.ebi.biostd.extended.model.ExtFile
import uk.ac.ebi.biostd.extended.model.ExtFileList
import uk.ac.ebi.biostd.extended.model.ExtFileTable
import uk.ac.ebi.biostd.extended.model.ExtFileType.FILE
import uk.ac.ebi.biostd.extended.model.ExtLink
import uk.ac.ebi.biostd.extended.model.ExtLinkTable
import uk.ac.ebi.biostd.extended.model.ExtSection
import uk.ac.ebi.biostd.extended.model.ExtSectionTable
import uk.ac.ebi.biostd.extended.model.FireFile
import uk.ac.ebi.extended.serialization.service.createExtFileList

object SectionFactory {
    fun defaultSection(
        accNo: String? = ACC_NO,
        type: String = TYPE,
        fileList: ExtFileList? = FILE_LIST,
        attributes: List<ExtAttribute> = ATTRIBUTES,
        sections: List<Either<ExtSection, ExtSectionTable>> = SECTIONS,
        files: List<Either<ExtFile, ExtFileTable>> = FILES,
        links: List<Either<ExtLink, ExtLinkTable>> = LINKS,
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
        firePath: String = FIRE_PATH,
        md5: String = MD5,
        size: Long = SIZE,
        attributes: List<ExtAttribute> = ATTRIBUTES,
    ) = FireFile(
        fireId = fireId,
        firePath = firePath,
        published = false,
        filePath = filePath,
        relPath = relPath,
        md5 = md5,
        size = size,
        type = FILE,
        attributes = attributes,
    )

    const val FILE_PATH = "folder/file.txt"
    const val REL_PATH = "Files/folder/file.txt"
    const val FIRE_ID = "fireId"
    const val FIRE_PATH = "submission/Files/folder/file.txt"
    const val MD5 = "md5"
    const val SIZE = 1L
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
        file = createExtFileList(files),
        filesUrl = filesUrl,
        pageTabFiles = pageTabFiles,
    )

    const val FILE_PATH = "folder/fileList.txt"
    val FILES = emptyList<ExtFile>()
    const val FILES_URL = "filesUrl"
    val PAGE_TAG_FILES = emptyList<ExtFile>()
}
