package ac.uk.ebi.biostd.persistence.test

import ac.uk.ebi.biostd.persistence.model.ReferencedFileList
import ebi.ac.uk.extended.model.ExtFileList
import org.assertj.core.api.Assertions.assertThat

internal val extFileList
    get() = ExtFileList("FileList", listOf(extTestRefFile))

internal fun assertDbRefFiles(fileList: ReferencedFileList?) {
    require(fileList != null)
    assertThat(fileList.name).isEqualTo("FileList")

    val refFiles = fileList.files.toList()
    assertDbRefFile(refFiles[0], extTestRefFile, 0)
}
