package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.NfsFile
import java.io.File

const val ROOT_FILE_LIST_FILE_NAME = "fileName1"
val ROOT_FILE_LIST_FILE = File("somePath1")
val rootSectionFileListFile = NfsFile(fileName = ROOT_FILE_LIST_FILE_NAME, file = ROOT_FILE_LIST_FILE)

const val SUB_FILE_LIST_FILE_NAME = "fileName2"
val SUB_FILE_LIST_FILE = File("somePath2")
val subSectionFileListFile = NfsFile(fileName = SUB_FILE_LIST_FILE_NAME, file = SUB_FILE_LIST_FILE)

const val ROOT_SEC_FILE_NAME = "fileName3"
val ROOT_SEC_FILE = File("somePath3")
val rootSectionFile = NfsFile(fileName = ROOT_SEC_FILE_NAME, file = ROOT_SEC_FILE)

const val ROOT_SEC_TABLE_FILE_NAME = "fileName4"
val ROOT_SEC_TABLE_FILE = File("somePath4")
val rootSectionTableFile = NfsFile(fileName = ROOT_SEC_TABLE_FILE_NAME, file = ROOT_SEC_TABLE_FILE)
