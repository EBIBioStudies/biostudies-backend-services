package ac.uk.ebi.biostd.test

import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.Link
import ac.uk.ebi.biostd.submission.Section

internal const val LINK_URL = "a link url"

fun simpleLink() = Link(url = LINK_URL)

internal const val FILE_NAME = "file name"
internal const val FILE_TYPE = "file type"
internal const val FILE_SIZE = 55

fun simpleFile() = File(name = FILE_NAME, type = FILE_TYPE, size = FILE_SIZE)

internal const val SEC_ACC_NO = "sec 123"
internal const val SEC_TYPE = "sec type"

fun simpleSection() = Section(accNo = SEC_ACC_NO, type = SEC_TYPE)