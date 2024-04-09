package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.NfsFile
import java.io.File

const val ROOT_FILE_LIST_FILE_NAME = "fileName1.txt"
const val ROOT_FILE_LIST_FILEPATH = "filePath1/fileName1"
const val ROOT_FILE_LIST_REL_NAME = "relPath1"
const val ROOT_FILE_LIST_FULL_PATH = "fullPath1"
val ROOT_FILE_LIST_FILE = File(ROOT_FILE_LIST_FILE_NAME)
val rootSectionFileListFile =
    NfsFile(
        filePath = ROOT_FILE_LIST_FILEPATH,
        relPath = ROOT_FILE_LIST_REL_NAME,
        fullPath = ROOT_FILE_LIST_FULL_PATH,
        file = ROOT_FILE_LIST_FILE,
        md5 = "abc-md5",
        size = 55L,
    )

const val SUB_FILE_LIST_FILE_NAME = "fileName2.txt"
const val SUB_FILE_LIST_FILEPATH = "filePath2/fileName2.txt"
const val SUB_FILE_LIST_REL_PATH = "relPath2"
const val SUB_FILE_LIST_FULL_PATH = "fullPath2"
val SUB_FILE_LIST_FILE = File(SUB_FILE_LIST_FILE_NAME)
val subSectionFileListFile =
    NfsFile(
        filePath = SUB_FILE_LIST_FILEPATH,
        relPath = SUB_FILE_LIST_REL_PATH,
        fullPath = SUB_FILE_LIST_FULL_PATH,
        file = SUB_FILE_LIST_FILE,
        md5 = "abc-md5",
        size = 55L,
    )

const val ROOT_SEC_FILE_NAME = "fileName3"
const val ROOT_SEC_FILEPATH = "filePath3/fileName3"
const val ROOT_SEC_REL_PATH = "relPath3"
const val ROOT_SEC_FULL_PATH = "fullPath3"
val ROOT_SEC_FILE = File("somePath3")
val rootSectionFile =
    NfsFile(
        filePath = ROOT_SEC_FILEPATH,
        relPath = ROOT_SEC_REL_PATH,
        fullPath = ROOT_SEC_FULL_PATH,
        file = ROOT_SEC_FILE,
        md5 = "abc-md5",
        size = 55L,
    )

const val ROOT_SEC_TABLE_FILE_NAME = "fileName4"
const val ROOT_SEC_TABLE_FILEPATH = "filePath4/fileName4"
const val ROOT_SEC_TABLE_REL_PATH = "relPath4"
const val ROOT_SEC_TABLE_FUL_PATH = "fullPath4"
val ROOT_SEC_TABLE_FILE = File("somePath4")
val rootSectionTableFile =
    NfsFile(
        filePath = ROOT_SEC_TABLE_FILEPATH,
        relPath = ROOT_SEC_TABLE_REL_PATH,
        fullPath = ROOT_SEC_TABLE_FUL_PATH,
        file = ROOT_SEC_TABLE_FILE,
        md5 = "abc-md5",
        size = 55L,
    )
