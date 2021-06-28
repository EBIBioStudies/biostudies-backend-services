package ac.uk.ebi.biostd.persistence.doc.test

import ac.uk.ebi.biostd.persistence.doc.model.DocFile
import ac.uk.ebi.biostd.persistence.doc.model.DocFileList
import ac.uk.ebi.biostd.persistence.doc.model.DocFileRef
import ac.uk.ebi.biostd.persistence.doc.model.FileSystem.NFS
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.assertBasicExtAttribute
import ac.uk.ebi.biostd.persistence.doc.test.AttributeTestHelper.basicDocAttribute
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.ExtFileList
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import org.assertj.core.api.Assertions.assertThat
import org.bson.types.ObjectId
import java.io.File

internal const val TEST_REL_PATH = "file.txt"
internal const val TEST_FULL_PATH = "/a/full/path/file.txt"
internal const val TEST_FILE_LIST = "file-list.tsv"
private const val TEST_MD5 = "a-test-md5"
private const val FILE_TYPE = "file"
private const val SIZE = 30L

internal object FileTestHelper {
    val docFile = DocFile(TEST_REL_PATH, TEST_FULL_PATH, listOf(basicDocAttribute), TEST_MD5, FILE_TYPE, SIZE, NFS)
    val docFileRef = DocFileRef(ObjectId(10, 10))
    val docFileList = DocFileList(TEST_FILE_LIST, listOf(docFileRef))

    fun assertExtFile(extFile: ExtFile, file: File) = when (extFile) {
        is FireFile -> TODO()
        is NfsFile -> assertNfsFile(extFile, file)
    }

    fun assertExtFileList(extFileList: ExtFileList) {
        assertThat(extFileList.fileName).isEqualTo(TEST_FILE_LIST)
        assertThat(extFileList.files).hasSize(0)
    }

    private fun assertNfsFile(nfsFile: NfsFile, file: File) {
        assertThat(nfsFile.fileName).isEqualTo(TEST_REL_PATH)
        assertThat(nfsFile.md5).isEqualTo(TEST_MD5)
        assertThat(nfsFile.file).isEqualTo(file)
        assertThat(nfsFile.attributes).hasSize(1)
        assertBasicExtAttribute(nfsFile.attributes.first())
    }
}
