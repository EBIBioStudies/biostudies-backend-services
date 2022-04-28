package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.FireDocFile
import ac.uk.ebi.biostd.persistence.doc.model.NfsDocFile
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertBasicExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileType.DIR
import ebi.ac.uk.extended.model.ExtFileType.FILE
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import org.assertj.core.api.Assertions.assertThat
import java.io.File

internal const val TEST_FILENAME = "file.txt"
internal const val TEST_FILEPATH = "folder/file.txt"
internal const val TEST_REL_PATH = "Files/folder/file.txt"
internal const val TEST_DIRECTORY = "fire-directory.txt"
internal const val TEST_FILEPATH_DIRECTORY = "filePath/folder/fire-directory.txt"
internal const val TEST_FULL_PATH = "/a/full/path/file.txt"
internal const val TEST_FILE_LIST = "file-list"
private const val TEST_MD5 = "a-test-md5"
private const val TEST_FIRE_FILE_ID = "fireFileId"
private const val TEST_FIRE_DIR_ID = "fireDirId"
private const val TEST_FIRE_FILE_SIZE = 10L
private const val SIZE = 30L

internal object FileTestHelper {
    val nfsDocFile =
        NfsDocFile(
            fileName = TEST_FILENAME,
            filePath = TEST_FILEPATH,
            relPath = TEST_REL_PATH,
            fullPath = TEST_FULL_PATH,
            attributes = listOf(basicDocAttribute),
            md5 = TEST_MD5,
            fileSize = SIZE,
            fileType = FILE.value
        )
    val fireDocFile =
        FireDocFile(
            fileName = TEST_FILENAME,
            filePath = TEST_FILEPATH,
            relPath = TEST_REL_PATH,
            fireId = TEST_FIRE_FILE_ID,
            attributes = listOf(basicDocAttribute),
            md5 = TEST_MD5,
            fileSize = TEST_FIRE_FILE_SIZE,
            fileType = FILE.value
        )
    val fireDocDirectory =
        FireDocFile(
            fileName = TEST_DIRECTORY,
            filePath = TEST_FILEPATH_DIRECTORY,
            relPath = TEST_REL_PATH,
            fireId = TEST_FIRE_DIR_ID,
            attributes = listOf(basicDocAttribute),
            md5 = TEST_MD5,
            fileSize = TEST_FIRE_FILE_SIZE,
            fileType = DIR.value
        )
    val docFileList = DocFileList(TEST_FILE_LIST)

    fun assertExtFile(extFile: ExtFile, file: File) = when (extFile) {
        is FireFile -> assertFireFile(extFile)
        is NfsFile -> assertNfsFile(extFile, file)
    }

    fun assertFireDirectory(fireFile: FireFile) {
        assertFireFileBasicAttributes(fireFile)
        assertThat(fireFile.type).isEqualTo(DIR)
        assertThat(fireFile.fileName).isEqualTo(TEST_DIRECTORY)
        assertThat(fireFile.filePath).isEqualTo(TEST_FILEPATH_DIRECTORY)
        assertThat(fireFile.relPath).isEqualTo(TEST_REL_PATH)
        assertThat(fireFile.fireId).isEqualTo(TEST_FIRE_DIR_ID)
    }

    private fun assertNfsFile(nfsFile: NfsFile, file: File) {
        assertThat(nfsFile.fileName).isEqualTo(TEST_FILENAME)
        assertThat(nfsFile.filePath).isEqualTo(TEST_FILEPATH)
        assertThat(nfsFile.relPath).isEqualTo(TEST_REL_PATH)
        assertThat(nfsFile.fullPath).isEqualTo(file.absolutePath)
        assertThat(nfsFile.md5).isEqualTo(file.md5())
        assertThat(nfsFile.file).isEqualTo(file)
        assertThat(nfsFile.attributes).hasSize(1)
        assertBasicExtAttribute(nfsFile.attributes.first())
    }

    private fun assertFireFile(fireFile: FireFile) {
        assertFireFileBasicAttributes(fireFile)
        assertThat(fireFile.type).isEqualTo(FILE)
        assertThat(fireFile.fileName).isEqualTo(TEST_FILENAME)
        assertThat(fireFile.filePath).isEqualTo(TEST_FILEPATH)
        assertThat(fireFile.relPath).isEqualTo(TEST_REL_PATH)
        assertThat(fireFile.fireId).isEqualTo(TEST_FIRE_FILE_ID)
    }

    private fun assertFireFileBasicAttributes(fireFile: FireFile) {
        assertThat(fireFile.md5).isEqualTo(TEST_MD5)
        assertThat(fireFile.size).isEqualTo(TEST_FIRE_FILE_SIZE)
        assertThat(fireFile.attributes).hasSize(1)
        assertBasicExtAttribute(fireFile.attributes.first())
    }
}
