package ac.uk.ebi.biostd.itest.assertions

import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionJson
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionTsv
import ac.uk.ebi.biostd.itest.factory.assertAllInOneSubmissionXml
import arrow.core.Either
import ebi.ac.uk.asserts.assertThat
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.ExtendedSubmission
import ebi.ac.uk.model.File
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.Section
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.attributeDetails
import ebi.ac.uk.util.collections.second
import ebi.ac.uk.util.collections.third
import org.assertj.core.api.Assertions.assertThat
import java.nio.file.Paths

internal class SubmissionAssertHelper(private val basePath: String) {
    internal fun assertSavedSubmission(accNo: String, submission: ExtendedSubmission) {
        assertThat(submission).hasAccNo(accNo)
        assertThat(submission).hasExactlyAttributes(
            Attribute("Title", "venous blood, Monocyte"), Attribute("ReleaseDate", "2021-02-12"))

        val rootSection = submission.section
        assertSections(rootSection)
        assertLinks(rootSection)
        assertDataFiles(rootSection, submission.relPath)
        assertSubmissionFiles(submission)
    }

    private fun assertSections(rootSection: Section) {
        assertThat(rootSection).has("SECT-001", "Study")
        assertThat(rootSection.attributes).containsExactly(
            Attribute("Project", "CEEHRC (McGill)"),
            Attribute("Organization", "Org1", true),
            Attribute(
                "Tissue type",
                "venous blood",
                false,
                attributeDetails("Tissue", "Blood"),
                attributeDetails("Ontology", "UBERON")))

        assertThat(rootSection.sections).hasSize(2)
        assertFirstSection(rootSection.sections[0])
        assertSecondSection(rootSection.sections[1])
    }

    private fun assertSecondSection(sectionEither: Either<Section, SectionsTable>) {
        val sectionTable = assertThat(sectionEither).isTable()
        assertThat(sectionTable.elements).hasSize(1)

        val sectionElement = sectionTable.elements[0]
        assertThat(sectionElement).has("DT-1", "Data")
        assertThat(sectionElement.attributes).containsExactly(
            Attribute("Title", "Group 1 Transcription Data"),
            Attribute("Description", "The data for zygotic transcription in mammals group 1"))
    }

    private fun assertFirstSection(sectionEither: Either<Section, SectionsTable>) {
        val section = assertThat(sectionEither).isSection()
        assertThat(section).has("SUBSECT-001", "Stranded Total RNA-Seq")

        val linksTable = assertThat(section.links[0]).isTable()
        assertThat(linksTable.elements).hasSize(1)

        val tableLink = linksTable.elements[0]
        assertThat(tableLink.url).isEqualTo("EGAD00001001282")
        assertThat(tableLink.attributes).containsExactly(
            Attribute("Type", "EGA"),
            Attribute("Assay type", "RNA-Seq"))
    }

    private fun assertLinks(rootSection: Section) {
        assertThat(rootSection.links).hasSize(1)
        val link = assertThat(rootSection.links[0]).isLink()
        assertThat(link).isEqualTo(Link("AF069309", listOf(Attribute("Type", "gen"))))
    }

    private fun assertDataFiles(section: Section, submissionRelPath: String) {
        assertThat(section.files).hasSize(2)
        val submissionFolderPath = "$basePath/submission/$submissionRelPath/Files"

        val file = assertThat(section.files.first()).isFile()
        assertFile(file, submissionFolderPath, "DataFile1.txt", Attribute("Description", "Data File 1"))

        val fileTable = assertThat(section.files.second()).isTable()
        assertThat(fileTable.elements).hasSize(3)

        assertFile(
            fileTable.elements.first(),
            submissionFolderPath,
            "DataFile2.txt",
            Attribute("Description", "Data File 2"),
            Attribute("Type", "Data"))

        assertFile(
            fileTable.elements.second(),
            submissionFolderPath,
            "Folder1/DataFile3.txt",
            Attribute("Description", "Data File 3"),
            Attribute("Type", "Data"))

        assertFile(
            fileTable.elements.third(),
            submissionFolderPath,
            "Folder1/Folder2/DataFile4.txt",
            Attribute("Description", "Data File 4"),
            Attribute("Type", "Data"))
    }

    private fun assertSubmissionFiles(submission: ExtendedSubmission) {
        val submissionFolderPath = "$basePath/submission/${submission.relPath}"
        val accNo = submission.accNo

        assertAllInOneSubmissionXml(getSubFileContent("$submissionFolderPath/$accNo.xml"), accNo)
        assertAllInOneSubmissionJson(getSubFileContent("$submissionFolderPath/$accNo.json"), accNo)
        assertAllInOneSubmissionTsv(getSubFileContent("$submissionFolderPath/$accNo.pagetab.tsv"), accNo)
    }

    private fun getSubFileContent(path: String): String {
        val filePath = Paths.get(path)
        assertThat(filePath).exists()

        return filePath.toFile().readText()
    }

    private fun assertFile(
        file: File,
        submissionFolderPath: String,
        expectedPath: String,
        vararg expectedAttrs: Attribute
    ) {
        assertThat(file.path).isEqualTo(expectedPath)
        assertThat(file.attributes).hasSize(expectedAttrs.size)
        assertThat(Paths.get("$submissionFolderPath/$expectedPath")).exists()
        expectedAttrs.forEachIndexed { index, attribute -> assertThat(file.attributes[index]).isEqualTo(attribute) }
    }
}
