package uk.ac.ebi.biostd.test

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
import uk.ac.ebi.biostd.model.extensions.rootPath
import uk.ac.ebi.biostd.model.extensions.title

fun createVenousBloodMonocyte() =
    submission("S-IHECRE00000919.1") {
        releaseDate = "2015-02-20"
        rootPath = "S-IHECRE00000919.1"
        title = "Submission title"
        attribute("DataSource", "BLUEPRINT")
        attribute("AttachTo", "blueprint")

        section("Study") {
            accNo = "SECT-001"
            attribute(name = "Title", value = "venous blood, \n Monocyte")
            attribute(name = "Project", value = "CEEHRC (McGill)")
            attribute(name = "Status", value = "Incomplete")
            attribute(name = "Organism", value = "Homo sapiens")
            attribute(
                name = "Tissue type",
                value = "venous blood",
                nameAttrs = mutableListOf(AttributeDetail("Tissue", "Blood")),
                valueAttrs = mutableListOf(AttributeDetail("Ontology", "UBERON")),
            )
            attribute(name = "Donor ID", value = "McGill0139")
            attribute(name = "Biomaterial Type", value = "primary cells")
            attribute(name = "Cell Type", value = "Monocyte", valueAttrs = mutableListOf(AttributeDetail("Ontology", "CL")))
            attribute(
                name = "Disease",
                value = "Systemic Lupus Erythematosus",
                valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")),
            )
            attribute(name = "Experiment type", value = "Single donor")

            link("IHECRE00000919.1") {
                attribute(name = "Type", value = "EpiRR")
            }

            file("TestData.csv") {
                attribute("Type", "data")
            }

            section("Stranded Total RNA-Seq") {
                accNo = "SUB-SECT-001"
                filesTable {
                    file("Results.xls") {
                        attribute("Type", "Results File")
                    }
                }

                linksTable {
                    link("EGAD00001001282") {
                        attribute(name = "Type", value = "EGA")
                        attribute(name = "Assay type", value = "RNA-Seq")
                        attribute(name = "Experiment type", value = "Stranded Total RNA-Seq")
                        attribute(name = "Primary id", value = "EGAX00001273202")
                    }
                }
            }

            sectionsTable {
                section("Data") { accNo = "DT-1" }
            }
        }
    }
