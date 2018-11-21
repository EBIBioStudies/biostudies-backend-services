package ac.uk.ebi.biostd.itest.factory

import net.soundvibe.jkob.json

fun allInOneSubmissionJson() = json {
    // TODO add support for accno (lowercase)
    "accNo" to "S-EPMC124"
    "type" to "submission"
    "attributes" [{
        "name" to "Title"
        "value" to "venous blood, Monocyte"
    }]
    "section" {
        "accNo" to "SECT-001"
        "type" to "Study"
        "attributes"[{
            "name" to "Project"
            "value" to "CEEHRC (McGill)"
        }, {
            "name" to "Tissue type"
            "value" to "venous blood"
            "nmqual"[{
                "name" to "Tissue"
                "value" to "Blood"
            }]
            "valqual"[{
                "name" to "Ontology"
                "value" to "UBERON"
            }]
        }]
        "links"[{
            "url" to "AF069309"
            "attributes"[{
                "name" to "Type"
                "value" to "gen"
            }]
        }]
        // TODO add support for subsections
        "sections" [{
            "accNo" to "SUBSECT-001"
            "type" to "Stranded Total RNA-Seq"
        }]
    }
}
