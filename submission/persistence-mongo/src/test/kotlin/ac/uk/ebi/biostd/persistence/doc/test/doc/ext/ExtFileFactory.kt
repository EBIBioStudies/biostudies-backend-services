package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtFile
import java.io.File

const val ROOT_FILE_LIST_FILE_NAME = "fileName1"
val ROOT_FILE_LIST_FILE = File("somePath1")
val rootSectionFileListFile = ExtFile(fileName = ROOT_FILE_LIST_FILE_NAME, file = ROOT_FILE_LIST_FILE)

const val SUB_FILE_LIST_FILE_NAME = "fileName2"
val SUB_FILE_LIST_FILE = File("somePath2")
val subSectionFileListFile = ExtFile(fileName = SUB_FILE_LIST_FILE_NAME, file = SUB_FILE_LIST_FILE)

const val ROOT_SEC_FILE_NAME = "fileName3"
val ROOT_SEC_FILE = File("somePath4")
val rootSectionFile = ExtFile(fileName = ROOT_SEC_FILE_NAME, file = ROOT_SEC_FILE)

const val ROOT_SEC_TABLE_FILE_NAME = "fileName4"
val ROOT_SEC_TABLE_FILE = File("somePath3")
val rootSectionTableFile = ExtFile(fileName = ROOT_SEC_TABLE_FILE_NAME, file = ROOT_SEC_TABLE_FILE)
