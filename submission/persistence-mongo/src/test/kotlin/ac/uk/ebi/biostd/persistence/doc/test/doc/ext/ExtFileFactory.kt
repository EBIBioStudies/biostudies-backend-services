package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.createNfsFile
import java.io.File

const val ROOT_FILE_LIST_FILE_NAME = "fileName1"
const val ROOT_FILE_LIST_FILEPATH = "filePath1/fileName1"
const val ROOT_FILE_LIST_REL_NAME = "relPath1"
const val ROOT_FILE_LIST_FULL_PATH = "fullPath1"
val ROOT_FILE_LIST_FILE = File("somePath1")
val rootSectionFileListFile = createNfsFile(
    ROOT_FILE_LIST_FILEPATH, ROOT_FILE_LIST_REL_NAME, file = ROOT_FILE_LIST_FILE
)

const val SUB_FILE_LIST_FILE_NAME = "fileName2"
const val SUB_FILE_LIST_FILEPATH = "filePath2/fileName2"
const val SUB_FILE_LIST_REL_PATH = "relPath2"
const val SUB_FILE_LIST_FULL_PATH = "fullPath2"
val SUB_FILE_LIST_FILE = File("somePath2")
val subSectionFileListFile =
    createNfsFile(
        SUB_FILE_LIST_FILEPATH,
        SUB_FILE_LIST_REL_PATH,
        file = SUB_FILE_LIST_FILE
    )

const val ROOT_SEC_FILE_NAME = "fileName3"
const val ROOT_SEC_FILEPATH = "filePath3/fileName3"
const val ROOT_SEC_REL_PATH = "relPath3"
const val ROOT_SEC_FULL_PATH = "fullPath3"
val ROOT_SEC_FILE = File("somePath3")
val rootSectionFile =
    createNfsFile(
        ROOT_SEC_FILEPATH,
        ROOT_SEC_REL_PATH,
        file = ROOT_SEC_FILE
    )

const val ROOT_SEC_TABLE_FILE_NAME = "fileName4"
const val ROOT_SEC_TABLE_FILEPATH = "filePath4/fileName4"
const val ROOT_SEC_TABLE_REL_PATH = "relPath4"
const val ROOT_SEC_TABLE_FUL_PATH = "fullPath4"
val ROOT_SEC_TABLE_FILE = File("somePath4")
val rootSectionTableFile =
    createNfsFile(
        ROOT_SEC_TABLE_FILEPATH,
        ROOT_SEC_TABLE_REL_PATH,
        file = ROOT_SEC_TABLE_FILE
    )
