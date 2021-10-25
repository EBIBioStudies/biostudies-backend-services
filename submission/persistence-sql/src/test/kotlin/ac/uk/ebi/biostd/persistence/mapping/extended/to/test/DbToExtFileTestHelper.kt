package ac.uk.ebi.biostd.persistence.mapping.extended.to.test

import ac.uk.ebi.biostd.persistence.model.DbFile
import ac.uk.ebi.biostd.persistence.model.DbReferencedFile
import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFile
import ebi.ac.uk.extended.model.FireDirectory
import ebi.ac.uk.extended.model.FireFile
import ebi.ac.uk.extended.model.NfsFile
import ebi.ac.uk.io.sources.BioFile
import ebi.ac.uk.io.sources.NfsBioFile
import org.assertj.core.api.Assertions.assertThat

internal const val FILE_NAME = "file.txt"

internal val fileDb get() = DbFile(FILE_NAME, 1, 55L, sortedSetOf(fileAttributeDb), false)

internal const val FILE_REF_NAME = "fileRefName"
internal val refRileDb get() = DbReferencedFile(FILE_REF_NAME, 0, 55L, sortedSetOf(refAttributeDb))

internal const val FILE_LIST_NAME = "fileList"
internal val refFileListDb get() = ReferencedFileList("fileList", sortedSetOf(refRileDb))

internal fun assertExtFile(extFile: ExtFile, bioFile: BioFile, fileName: String) = when (extFile) {
    is FireFile -> TODO()
    is FireDirectory -> TODO()
    is NfsFile -> assertNfsFile(extFile, bioFile, fileName)
}

private fun assertNfsFile(nfsFile: NfsFile, bioFile: BioFile, filePath: String) {
    bioFile as NfsBioFile
    assertThat(nfsFile.fileName).isEqualTo(filePath.substringAfterLast("/"))
    assertThat(nfsFile.filePath).isEqualTo(filePath)
    assertThat(nfsFile.relPath).isEqualTo("Files/$filePath")
    assertThat(nfsFile.file).isEqualTo(bioFile.file)
    assertThat(nfsFile.attributes).hasSize(1)
    assertExtAttribute(nfsFile.attributes.first())
}
