package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.util.collections.ifNotEmpty
import org.redundent.kotlin.xml.xml
import java.time.Instant

internal data class Contributor(
    val name: String,
    val surname: String,
    val affiliation: String,
    val orcid: String?
)

internal class DoiRequest(
    private val accNo: String,
    private val title: String,
    private val instanceUrl: String,
    private val contributors: List<Contributor>,
) {
    val doi: String
        get() = "$BS_DOI_ID/$accNo"

    fun asXmlRequest(): String {
        val timestamp = Instant.now().epochSecond.toString()
        return xml("doi_batch") {
            xmlns = "http://www.crossref.org/schema/4.4.1"
            "head" {
                "doi_batch_id" { -timestamp }
                "timestamp" { -timestamp }
                "depositor" {
                    "depositor_name" { -DEPOSITOR }
                    "email_address" { -EMAIL }
                }
                "registrant" { -DEPOSITOR }
            }
            "body" {
                "database" {
                    "database_metadata" {
                        attribute("language", "en")
                        "titles" {
                            "title" { -BS_TITLE }
                        }
                    }
                    "dataset" {
                        contributors.ifNotEmpty {
                            "contributors" {
                                contributors.forEachIndexed { index, contributor ->
                                    "person_name" {
                                        attribute("contributor_role", "author")
                                        attribute("sequence", index)
                                        "given_name" { -contributor.name }
                                        "surname" { -contributor.surname }
                                        "affiliation" { -contributor.affiliation }
                                        contributor.orcid?.let { orcid ->
                                            "ORCID" {
                                                attribute("authenticated", "false")
                                                -orcid
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        "titles" {
                            "title" { -title }
                        }
                        "doi_data" {
                            "doi" { -doi }
                            "resource" { -"$instanceUrl/studies/$accNo" }
                        }
                    }
                }
            }
        }.toString()
    }

    companion object {
        const val BS_DOI_ID = "10.6019"
        const val BS_TITLE = "BioStudies Database"
        const val DEPOSITOR = "EMBL-EBI"
        const val EMAIL = "biostudies@ebi.ac.uk"
    }
}
