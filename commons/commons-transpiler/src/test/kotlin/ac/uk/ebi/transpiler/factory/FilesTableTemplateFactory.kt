package ac.uk.ebi.transpiler.factory

import ac.uk.ebi.transpiler.common.FilesTableTemplate
import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv

fun testTemplate() =
    tsv {
        line("Plate", "Replicate", "Well", "Gene Identifier", "Gene Symbol")
        line("Plate1", "rep1", "A01", "ynl003c", "pet8")
        line("Plate2", "rep2", "A02", "ybl104c", "sea4")
        line("")
    }

fun filesTableTemplate(): FilesTableTemplate {
    val template = FilesTableTemplate()
    template.header = listOf("Plate", "Replicate", "Well", "Gene Identifier", "Gene Symbol")
    template.addRecord("Plate1/rep1/A01", listOf("Plate1", "rep1", "A01", "ynl003c", "pet8"))
    template.addRecord("Plate2/rep2/A02", listOf("Plate2", "rep2", "A02", "ybl104c", "sea4"))

    return template
}
