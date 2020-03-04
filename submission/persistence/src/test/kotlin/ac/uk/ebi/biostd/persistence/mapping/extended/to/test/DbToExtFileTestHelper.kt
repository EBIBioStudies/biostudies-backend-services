package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFile
import org.assertj.core.api.Assertions.assertThat
import java.io.File as SystemFile

internal const val FILE_NAME = "fileName"
internal const val MD5 = "md5"

internal val fileDb get() = DbFile(FILE_NAME, 1, MD5, 55L, sortedSetOf(fileAttributeDb))

internal const val FILE_REF_NAME = "fileRefName"
internal val refRileDb get() = DbReferencedFile(FILE_REF_NAME, 0, 55L, sortedSetOf(refAttributeDb))

internal const val FILE_LIST_NAME = "fileList"
internal val refFileListDb get() = ReferencedFileList("fileList", sortedSetOf(refRileDb))

internal fun assertExtFile(extFile: ExtFile, systemFile: SystemFile, fileName: String) {
    assertThat(extFile.fileName).isEqualTo(fileName)
    assertThat(extFile.file).isEqualTo(systemFile)

    assertThat(extFile.attributes).hasSize(1)
    assertExtAttribute(extFile.attributes.first())
}
