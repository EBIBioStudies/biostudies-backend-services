package ac.uk.ebi.biostd.persistence.pagetab

import ebi.ac.uk.io.RW_R_____
import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Files

fun verifySubmissionFiles(subFolder: File) {
    assertPageTabFile(subFolder.resolve(SUB_JSON))
    assertPageTabFile(subFolder.resolve(SUB_XML))
    assertPageTabFile(subFolder.resolve(SUB_TSV))
}

fun verifyFileLists(subFolder: File) {
    val submissionFiles = subFolder.resolve("Files")
    assertPageTabFile(submissionFiles.resolve("data/${FILE_LIST_JSON}"))
    assertPageTabFile(submissionFiles.resolve("data/${FILE_LIST_XML}"))
    assertPageTabFile(submissionFiles.resolve("data/${FILE_LIST_TSV}"))
}
private fun assertPageTabFile(file: File) {
    assertThat(file).exists()
    assertThat(Files.getPosixFilePermissions(file.toPath())).containsExactlyInAnyOrderElementsOf(RW_R_____)
}
const val SUB_JSON = "S-TEST123.json"
const val SUB_XML = "S-TEST123.xml"
const val SUB_TSV = "S-TEST123.pagetab.tsv"
const val FILE_LIST_JSON = "file-list.json"
const val FILE_LIST_XML = "file-list.xml"
const val FILE_LIST_TSV = "file-list.pagetab.tsv"