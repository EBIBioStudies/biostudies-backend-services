package ac.uk.ebi.biostd.test

import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.filesTable
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.sectionsTable
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.extensions.releaseDate
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title

fun createVenousBloodMonocyte() = submission("S-IHECRE00000919.1") {
    releaseDate = "2015-02-20"
    rootPath = "S-IHECRE00000919.1"
    title = "Submission title"
    attribute("DataSource", "BLUEPRINT")
    attribute("AttachTo", "blueprint")

    section("Study") {
        accNo = "SECT-001"
        attribute(name = "Title", value = "venous blood, Monocyte")
        attribute(name = "Project", value = "CEEHRC (McGill)")
        attribute(name = "Status", value = "Incomplete")
        attribute(name = "Organism", value = "Homo sapiens")
        attribute(
            name = "Tissue type",
            value = "venous blood",
            nameAttrs = mutableListOf(AttributeDetail("Tissue", "Blood")),
            valueAttrs = mutableListOf(AttributeDetail("Ontology", "UBERON")))
        attribute(name = "Donor ID", value = "McGill0139")
        attribute(name = "Biomaterial Type", value = "primary cells")
        attribute(name = "Cell Type", value = "Monocyte", valueAttrs = mutableListOf(AttributeDetail("Ontology", "CL")))
        attribute(
            name = "Disease",
            value = "Systemic Lupus Erythematosus",
            valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")))
        attribute(name = "Experiment type", value = "Single donor")

        link("IHECRE00000919.1") {
            attribute(name = "Type", value = "EpiRR")
        }

        file("TestData.csv") {
            attribute("Type", "data")
        }

        section("Stranded Total RNA-Seq") {
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
