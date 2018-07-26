package ac.uk.ebi.biostd.test

import ac.uk.ebi.biostd.submission.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

val releaseDate: Instant = LocalDateTime.parse("2015-02-20T06:30:00").toInstant(ZoneOffset.UTC)


fun createVenousBloodMonocyte(): Submission {
    return submission {
        accNo = "S-IHECRE00000919.1"
        title = "Submission title"
        rTime = releaseDate.epochSecond
        accessTags = mutableListOf("Public")
        rootPath = "S-IHECRE00000919.1"

        attribute("DataSource", "BLUEPRINT")
        attribute("AttachTo", "blueprint")

        section {
            type = "Study"

            attribute(name = "Title", value = "venous blood, Monocyte")
            attribute(name = "Project", value = "CEEHRC (McGill)")
            attribute(name = "Status", value = "Incomplete")
            attribute(name = "Organism", value = "Homo sapiens")
            attribute(name = "Tissue type", value = "venous blood", qualifierVal = "Ontology=UBERON")
            attribute(name = "Donor ID", value = "McGill0139")
            attribute(name = "Biomaterial Type", value = "primary cells")
            attribute(name = "Cell Type", value = "Monocyte", qualifierVal = "Ontology=CL")
            attribute(name = "Disease", value = "Systemic Lupus Erythematosus", qualifierVal = "Ontology=EFO")
            attribute(name = "Experiment type", value = "Single donor")

            link {
                url = "IHECRE00000919.1"
                attribute(name = "Type", value = "EpiRR")
            }
        }

        section {
            type = "Stranded Total RNA-Seq"

            table {
                link {
                    url = "EGAD00001001282"
                    attribute(name = "Type", value = "EGA")
                    attribute(name = "Assay type", value = "RNA-Seq")
                    attribute(name = "Experiment type", value = "Stranded Total RNA-Seq")
                    attribute(name = "Primary id", value = "EGAX00001273202")
                }
            }
        }
    }
}

fun createRNA_Profiling(): Submission {
    return submission {
        accNo = "E-MTAB-6957"
        accessTags = mutableListOf("Public")

        rootPath = "E-MTAB/E-MTAB-6957"
        attribute("AttachTo", "blueprint")

        section {
            type = "Study"
            accNo = "s-E-MTAB-6957"

            section {
                type = "MinSeq Score"

                attribute("Exp. Design", "*")
                attribute("Protocols", "*")
                attribute("Variables", "*")
                attribute("Processed", "-")
                attribute("Raw", "-")
            }

            section {
                type = "Author"

                attribute("Name", "Bram Boeckx")
                attribute("Email", "bram.boeckx@vib-kuleuven.be")
                attribute("<affiliation>", value = "o1", reference = true)
            }

            section {
                type = "Organization"
                accNo = "o1"

                attribute("Name", "VIB Center for Cancer Biology (CCB)  Laboratory for Translational Genetics  Department of Human Genetics")
                attribute("Address", "VIB Center for Cancer Biology (CCB) KULeuven Campus Gasthuisberg, O&N4 Herestraat 49-B912 B-3000 Leuven, Belgium")
            }

            section {
                type = "Experiment Protocols"
                accNo = "protocols-E-MTAB-6957"

                table {
                    section {
                        accNo = "P-MTAB-76451"
                        attribute(name = "Name", value = "P-MTAB-76451")
                        attribute(name = "Type", value = "nucleic acid extraction protocol")
                        attribute(name = "Description", value = "Total RNA was extracted from six 30-μm sections of frozen whole tumor and adjacent lung tissue using the Qiagen-RNeasy Mini kit according to the supplier’s instructions (Qiagen, Hilden, Germany). The quantity of DNA-free total RNA was measured using a Nanodrop-2000.")
                    }

                    section {
                        accNo = "P-MTAB-76452"
                        attribute(name = "Name", value = "P-MTAB-76452")
                        attribute(name = "Type", value = "nucleic acid library construction protocol")
                        attribute(name = "Description", value = "Illumina sequencing libraries were prepared according to the TruSeq RNA Sample Preparation Guide following the manufacturer’s instructions.")
                    }

                    section {
                        accNo = "P-MTAB-76450"
                        attribute(name = "Name", value = "P-MTAB-76450")
                        attribute(name = "Type", value = "sample collection protocol")
                        attribute(name = "Description", value = "Study participants and phenotypes Between March 2010 and August 2011, we prospectively recruited newly diagnosed and previously untreated lung cancer patients that were referred for thoracic surgery with curative intent. Subjects were only eligible for inclusion if surgical resection of the tumor occurred prior to any form of medical treatment. Tissue was collected immediately after lobectomy/pneumonectomy.")
                    }

                    section {
                        accNo = "P-MTAB-76453"
                        attribute(name = "Name", value = "P-MTAB-76453")
                        attribute(name = "Type", value = "nucleic acid sequencing protocol")
                        attribute(name = "Description", value = "Libraries were sequenced on a Illumina HiSeq2000 sequencer generating single end 51bp reads.")
                        attribute(name = "Hardware", value = "Illumina HiSeq 2000")
                    }
                }
            }
        }
    }
}
