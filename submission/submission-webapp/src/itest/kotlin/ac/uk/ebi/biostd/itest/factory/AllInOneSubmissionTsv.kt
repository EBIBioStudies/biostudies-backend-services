package ac.uk.ebi.biostd.itest.factory

import ebi.ac.uk.dsl.tsv.line
import ebi.ac.uk.dsl.tsv.tsv
import ebi.ac.uk.test.createFile
import io.github.glytching.junit.extension.folder.TemporaryFolder

fun submissionSpecTsv(tempFolder: TemporaryFolder, accNo: String): SubmissionSpec = SubmissionSpec(
    submission = tempFolder.createFile("submission.tsv", allInOneSubmissionTsv(accNo).toString()),
    fileList = tempFolder.createFile("file-list.tsv", fileList().toString()),
    files = submissionsFiles(tempFolder),
    subFileList = SubmissionFile(tempFolder.createFile("file-list2.tsv", fileList2().toString()), "sub-folder")
)

fun allInOneSubmissionTsv(accNo: String) = tsv {
    line("# All in one submission")
    line("Submission", accNo)
    line("Title", "venous blood, ∆Monocyte")
    line("ReleaseDate", "2021-02-12")
    line()

    line("Study", "SECT-001")
    line("Project", "CEEHRC (McGill)")
    line("<Organization>", "Org1")
    line("Tissue type", "venous blood")
    line("[Ontology]", "UBERON")
    line("(Tissue)", "Blood")
    line("File List", "file-list.tsv")
    line()

    line("Link", "AF069309")
    line("type", "gen")
    line()

    line("File", "DataFile1.txt")
    line("Description", "Data File 1")
    line()

    line("Files", "Description", "Type")
    line("DataFile2.txt", "Data File 2", "Data")
    line("Folder1/DataFile3.txt", "Data File 3", "Data")
    line("Folder1/Folder2/DataFile4.txt", "Data File 4", "Data")
    line()

    line("Stranded Total RNA-Seq", "SUBSECT-001", "SECT-001")
    line("File List", "sub-folder/file-list2.tsv")
    line()

    line("Data[SECT-001]", "Title", "Description")
    line("DT-1", "Group 1 Transcription Data", "The data for zygotic transcription in mammals group 1")
    line()

    line("Links", "Type", "Assay type", "(TermId)", "[Ontology]")
    line("EGAD00001001282", "EGA", "RNA-Seq", "EFO_0002768", "EFO")
    line()
}

private fun fileList() = tsv {
    line("Files", "Type")
    line("DataFile5.txt", "referenced")
    line("Folder1/DataFile6.txt", "referenced")
}

private fun fileList2() = tsv {
    line("Files", "Type")
    line("DataFile7.txt", "referenced")
    line("Folder1/DataFile8.txt", "referenced")
}
