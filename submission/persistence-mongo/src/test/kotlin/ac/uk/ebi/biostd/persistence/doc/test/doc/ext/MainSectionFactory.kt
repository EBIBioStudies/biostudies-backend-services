package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable
import uk.ac.ebi.extended.serialization.service.createFileList

const val ROOT_SEC_ACC_NO = "accNo-1"
const val SUB_SEC_ACC_NO = "accNo-2"
const val SUB_SEC_TABLE_ACC_NO3 = "accNo-3"
const val ROOT_SEC_TYPE = "Study1"
const val SUB_SEC_TYPE = "Study2"
const val SUB_SEC_TABLE_TYPE = "Study3"

const val ROOT_SEC_EXT_FILE_LIST_PATH = "listFileName1"
const val SUB_SEC_EXT_FILE_LIST_PATH = "folder/listFileName2"

val subSection = ExtSection(
    accNo = SUB_SEC_ACC_NO,
    type = SUB_SEC_TYPE,
    fileList = ExtFileList(
        filePath = SUB_SEC_EXT_FILE_LIST_PATH,
        file = createFileList(listOf(subSectionFileListFile)),
    )
)

val subSectionTable = ExtSection(
    accNo = SUB_SEC_TABLE_ACC_NO3,
    type = SUB_SEC_TABLE_TYPE,
    fileList = null,
    attributes = listOf(subSectionTableAttribute)
)

const val ROOT_SECTION_LINK_URL = "url1"
val rootSectionLink = ExtLink(
    url = ROOT_SECTION_LINK_URL,
    attributes = listOf(rootSectionLinkAttribute)
)
const val ROOT_SECTION_TABLE_LINK_URL = "url2"
val rootSectionTableLink = ExtLink(
    url = ROOT_SECTION_TABLE_LINK_URL,
    attributes = listOf(rootSectionTableLinkAttribute)
)

val rootSection = ExtSection(
    accNo = ROOT_SEC_ACC_NO,
    type = ROOT_SEC_TYPE,
    fileList = ExtFileList(
        filePath = ROOT_SEC_EXT_FILE_LIST_PATH,
        file = createFileList(listOf(rootSectionFileListFile))
    ),
    attributes = listOf(rootSectionAttribute),
    sections = listOf(
        Either.left(subSection),
        Either.right(ExtSectionTable(sections = listOf(subSectionTable)))
    ),
    files = listOf(
        Either.left(rootSectionFile),
        Either.right(ExtFileTable(files = listOf(rootSectionTableFile)))
    ),
    links = listOf(
        Either.left(rootSectionLink),
        Either.right(ExtLinkTable(links = listOf(rootSectionTableLink)))
    )
)
