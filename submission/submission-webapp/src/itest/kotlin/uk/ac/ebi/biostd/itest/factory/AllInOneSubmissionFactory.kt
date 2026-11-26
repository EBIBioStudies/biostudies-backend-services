package uk.ac.ebi.biostd.itest.factory

import uk.ac.ebi.biostd.dsl.attribute
import uk.ac.ebi.biostd.dsl.file
import uk.ac.ebi.biostd.dsl.filesTable
import uk.ac.ebi.biostd.dsl.link
import uk.ac.ebi.biostd.dsl.linksTable
import uk.ac.ebi.biostd.dsl.section
import uk.ac.ebi.biostd.dsl.sectionsTable
import uk.ac.ebi.biostd.dsl.submission
import uk.ac.ebi.biostd.model.AttributeDetail
import uk.ac.ebi.biostd.model.extensions.releaseDate
import uk.ac.ebi.biostd.model.extensions.title
import uk.ac.ebi.biostd.util.date.toStringDate
import java.time.OffsetDateTime

fun allInOneSubmission(accNo: String) =
    submission(accNo) {
        title = "venous blood, ∆Monocyte"
        releaseDate = OffsetDateTime.now().toStringDate()

        section("Study") {
            this.accNo = "SECT-001"
            attribute("Project", "CEEHRC (McGill)")
            attribute("Organization", "Org1", ref = true)
            attribute(
                name = "Tissue type",
                value = "venous blood",
                ref = false,
                nameAttrs = mutableListOf(AttributeDetail("Tissue", "Blood")),
                valueAttrs = mutableListOf(AttributeDetail("Ontology", "UBERON")),
            )

            link("AF069309") {
                attribute("type", "gen")
            }

            file("DataFile1.txt") {
                size = 9
                attribute("Description", "Data File 1")
            }

            filesTable {
                file("DataFile2.txt") {
                    size = 9
                    attribute("Description", "Data File 2")
                    attribute("Type", "Data")
                }
                file("Folder1/DataFile3.txt") {
                    size = 9
                    attribute("Description", "Data File 3")
                    attribute("Type", "Data")
                }
                file("Folder1/Folder2/DataFile4.txt") {
                    size = 9
                    attribute("Description", "Data File 4")
                    attribute("Type", "Data")
                }
            }

            section("Stranded Total RNA-Seq") {
                this.accNo = "SUBSECT-001"

                linksTable {
                    link("EGAD00001001282") {
                        attribute("Type", "EGA")
                        attribute(
                            name = "Assay type",
                            value = "RNA-Seq",
                            ref = false,
                            nameAttrs = mutableListOf(AttributeDetail("TermId", "EFO_0002768")),
                            valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
                        )
                    }
                }
            }

            sectionsTable {
                section("Data") {
                    this.accNo = "DT-1"
                    attribute("Title", "Group 1 Transcription Data")
                    attribute("Description", "The data for zygotic transcription in mammals group 1")
                }
            }
        }
    }

fun allInOneRootSection() =
    section("Study") {
        this.accNo = "SECT-001"

        attribute("Project", "CEEHRC (McGill)")
        attribute("Organization", "Org1")
        attribute("Tissue type", "venous blood")
    }

fun allInOneRootSectionLink() =
    link("AF069309") {
        attribute("type", "gen")
    }

fun allInOneRootSectionFile() =
    file("DataFile1.txt") {
        size = 9
        attribute("Description", "Data File 1")
    }

fun allInOneRootSectionFilesTable() =
    filesTable {
        file("DataFile2.txt") {
            size = 9
            attribute("Description", "Data File 2")
            attribute("Type", "Data")
        }
        file("Folder1/DataFile3.txt") {
            size = 9
            attribute("Description", "Data File 3")
            attribute("Type", "Data")
        }
        file("Folder1/Folder2/DataFile4.txt") {
            size = 9
            attribute("Description", "Data File 4")
            attribute("Type", "Data")
        }
    }

fun allInOneSubsection() =
    section("Stranded Total RNA-Seq") {
        this.accNo = "SUBSECT-001"
        attribute("File List", "sub-folder/file-list2.json")
    }

fun allInOneSubSectionLinksTable() =
    linksTable {
        link("EGAD00001001282") {
            attribute("Type", "EGA")
            attribute("Assay type", "RNA-Seq")
        }
    }

fun allInOneSubSectionsTable() =
    sectionsTable {
        section("Data") {
            this.accNo = "DT-1"
            attribute("Title", "Group 1 Transcription Data")
            attribute("Description", "The data for zygotic transcription in mammals group 1")
        }
    }
