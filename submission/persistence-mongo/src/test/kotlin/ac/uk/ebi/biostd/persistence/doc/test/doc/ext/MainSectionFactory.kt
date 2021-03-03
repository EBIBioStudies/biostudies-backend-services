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
        files = listOf(extFileSubsectionFileList)
    )
)

val subSectionTable = ExtSection(
    accNo = SEC_ACC_NO3,
    type = SEC_TYPE3,
    fileList = null,
    attributes = listOf(extAttribute2)
)

const val EXT_LINK_URL1 = "url1"
val extLink1 = ExtLink(
    url = EXT_LINK_URL1,
    attributes = listOf(extAttribute3)
)
const val EXT_LINK_URL2 = "url2"
val extLink2 = ExtLink(
    url = EXT_LINK_URL2,
    attributes = listOf(extAttribute4)
)

val mainSection = ExtSection(
    accNo = SEC_ACC_NO1,
    type = SEC_TYPE1,
    fileList = ExtFileList(
        fileName = EXT_FILE_LIST_FILENAME1,
        files = listOf(extFileMainSectionFileList)
    ),
    attributes = listOf(extAttribute1),
    sections = listOf(
        Either.left(subSection),
        Either.right(ExtSectionTable(sections = listOf(subSectionTable)))
    ),
    files = listOf(
        Either.left(extFile3),
        Either.right(ExtFileTable(files = listOf(extFile4)))
    ),
    links = listOf(
        Either.left(extLink1),
        Either.right(ExtLinkTable(links = listOf(extLink2)))
    )
)
