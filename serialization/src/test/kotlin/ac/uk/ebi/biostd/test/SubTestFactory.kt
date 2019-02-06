package ac.uk.ebi.biostd.test

import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.linksTable
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.AttributeDetail
import ebi.ac.uk.model.Submission
import ebi.ac.uk.model.extensions.releaseTime
import ebi.ac.uk.model.extensions.rootPath
import ebi.ac.uk.model.extensions.title
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

val releaseDate: Instant = LocalDateTime.parse("2015-02-20T06:30:00").toInstant(ZoneOffset.UTC)

fun createVenousBloodMonocyte(): Submission {
    return submission("S-IHECRE00000919.1") {
        releaseTime = releaseDate
        rootPath = "S-IHECRE00000919.1"
        title = "Submission title"
        attribute("DataSource", "BLUEPRINT")
        attribute("AttachTo", "blueprint")

        section("Study") {

            attribute(name = "Title", value = "venous blood, Monocyte")
            attribute(name = "Project", value = "CEEHRC (McGill)")
            attribute(name = "Status", value = "Incomplete")
            attribute(name = "Organism", value = "Homo sapiens")
            attribute(name = "Tissue type", value = "venous blood", valueAttrs = mutableListOf(AttributeDetail("Ontology", "UBERON")))
            attribute(name = "Donor ID", value = "McGill0139")
            attribute(name = "Biomaterial Type", value = "primary cells")
            attribute(name = "Cell Type", value = "Monocyte", valueAttrs = mutableListOf(AttributeDetail("Ontology", "CL")))
            attribute(name = "Disease", value = "Systemic Lupus Erythematosus", valueAttrs = mutableListOf(AttributeDetail("Ontology", "EFO")))
            attribute(name = "Experiment type", value = "Single donor")

            link("IHECRE00000919.1") {
                attribute(name = "Type", value = "EpiRR")
            }

            section("Stranded Total RNA-Seq") {

                linksTable {
                    link("EGAD00001001282") {
                        attribute(name = "Type", value = "EGA")
                        attribute(name = "Assay type", value = "RNA-Seq")
                        attribute(name = "Experiment type", value = "Stranded Total RNA-Seq")
                        attribute(name = "Primary id", value = "EGAX00001273202")
                    }
                }
            }
        }
    }
}
