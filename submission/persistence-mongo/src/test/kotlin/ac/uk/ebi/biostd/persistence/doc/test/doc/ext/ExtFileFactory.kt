package ac.uk.ebi.biostd.persistence.doc.test.doc.ext

import ebi.ac.uk.extended.model.ExtFile
import java.io.File

const val EXT_FILE_FILENAME1 = "fileName1"
const val EXT_FILE_FILENAME2 = "fileName2"
const val EXT_FILE_FILENAME3 = "fileName3"
const val EXT_FILE_FILENAME4 = "fileName4"

val EXT_FILE_FILE1 = File("somePath1")
val extFileMainSectionFileList = ExtFile(fileName = EXT_FILE_FILENAME1, file = EXT_FILE_FILE1)
val EXT_FILE_FILE2 = File("somePath2")
val extFileSubsectionFileList = ExtFile(fileName = EXT_FILE_FILENAME2, file = EXT_FILE_FILE2)

val EXT_FILE_FILE4 = File("somePath3")
val EXT_FILE_FILE3 = File("somePath4")
val extFile4 = ExtFile(fileName = EXT_FILE_FILENAME4, file = EXT_FILE_FILE4)
val extFile3 = ExtFile(fileName = EXT_FILE_FILENAME3, file = EXT_FILE_FILE3)
