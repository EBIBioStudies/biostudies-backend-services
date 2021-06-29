package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.ext.md5
import ebi.ac.uk.io.ext.size
import org.assertj.core.api.Assertions.assertThat
import java.io.File as SystemFile

internal const val FILE_NAME = "fileName"

internal val fileDb get() = DbFile(FILE_NAME, 1, 55L, sortedSetOf(fileAttributeDb), false)

internal const val FILE_REF_NAME = "fileRefName"
internal val refRileDb get() = DbReferencedFile(FILE_REF_NAME, 0, 55L, sortedSetOf(refAttributeDb))

internal const val FILE_LIST_NAME = "fileList"
internal val refFileListDb get() = ReferencedFileList("fileList", sortedSetOf(refRileDb))

internal fun assertExtFile(extFile: ExtFile, systemFile: SystemFile, fileName: String) = when (extFile) {
    is FireFile -> TODO()
    is NfsFile -> assertNfsFile(extFile, systemFile, fileName)
}

private fun assertNfsFile(nfsFile: NfsFile, systemFile: SystemFile, fileName: String) {
    assertThat(nfsFile.file).isEqualTo(systemFile)
    assertThat(nfsFile.fileName).isEqualTo(fileName)
    assertThat(nfsFile.md5).isEqualTo(systemFile.md5())
    assertThat(nfsFile.size).isEqualTo(systemFile.size())

    assertThat(nfsFile.attributes).hasSize(1)
    assertExtAttribute(nfsFile.attributes.first())
}
