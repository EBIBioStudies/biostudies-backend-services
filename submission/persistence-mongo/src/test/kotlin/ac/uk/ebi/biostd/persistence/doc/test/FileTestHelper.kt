package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertBasicExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import org.assertj.core.api.Assertions.assertThat
import java.io.File

internal const val TEST_PATH = "file.txt"
internal const val TEST_MD5 = "a-test-md5"
internal const val TEST_FILE_LIST = "file-list.tsv"

internal object FileTestHelper {
    val docFile = DocFile(TEST_PATH, listOf(basicDocAttribute), TEST_MD5)
    val docFileList = DocFileList(TEST_FILE_LIST, listOf(docFile))

    fun assertExtFile(extFile: ExtFile, file: File) {
        assertThat(extFile.fileName).isEqualTo(TEST_PATH)
        assertThat(extFile.md5).isEqualTo(TEST_MD5)
        assertThat(extFile.file).isEqualTo(file)
        assertThat(extFile.attributes).hasSize(1)
        assertBasicExtAttribute(extFile.attributes.first())
    }

    fun assertExtFileList(extFileList: ExtFileList, file: File) {
        assertThat(extFileList.fileName).isEqualTo(TEST_FILE_LIST)
        assertThat(extFileList.files).hasSize(1)
        assertExtFile(extFileList.files.first(), file)
    }
}
