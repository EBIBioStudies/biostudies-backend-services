package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import arrow.core.Either
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.ExtFileTable
import ebi.ac.uk.extended.model.ExtLink
import ebi.ac.uk.extended.model.ExtLinkTable
import ebi.ac.uk.extended.model.ExtSection
import ebi.ac.uk.extended.model.ExtSectionTable

const val SEC_ACC_NO1 = "accNo-1"
const val SEC_ACC_NO2 = "accNo-2"
const val SEC_ACC_NO3 = "accNo-3"
const val SEC_TYPE1 = "Study1"
const val SEC_TYPE2 = "Study2"
const val SEC_TYPE3 = "Study3"

const val EXT_FILE_LIST_FILENAME1 = "listFileName1"
const val EXT_FILE_LIST_FILENAME2 = "listFileName2"

val subSection = ExtSection(
    accNo = SEC_ACC_NO2,
    type = SEC_TYPE2,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME2,
        files = listOf(subSectionFileListFile)
    )
)

val subSectionTable = ExtSection(
    accNo = SEC_ACC_NO3,
    type = SEC_TYPE3,
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

val mainSection = ExtSection(
    accNo = SEC_ACC_NO1,
    type = SEC_TYPE1,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME1,
        files = listOf(rootSectionFileListFile)
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
