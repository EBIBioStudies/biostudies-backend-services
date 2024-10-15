package ac.uk.ebi.biostd.submission.model

import ebi.ac.uk.util.collections.ifNotEmpty
import org.redundent.kotlin.xml.PrintOptions
import org.redundent.kotlin.xml.xml

data class Contributor(
    val name: String?,
    val surname: String,
    val affiliation: String,
    val orcid: String?,
)

class DoiRequest(
    private val accNo: String,
    private val title: String,
    private val email: String,
    private val timestamp: String,
    private val instanceUrl: String,
    private val contributors: List<Contributor>,
) {
    val doi: String
        get() = "$BS_DOI_ID/$accNo"

    fun asXmlRequest(): String {
        return xml("doi_batch") {
            xmlns = XML_NAMESPACE
            attribute("xmlns:xsi", XML_SCHEMA_INSTANCE)
            attribute("version", XML_SCHEMA_VERSION)
            attribute("xsi:schemaLocation", "$XML_SCHEMA_LOCATION_1 $XML_SCHEMA_LOCATION_2")
            "head" {
                "doi_batch_id" { -timestamp }
                "timestamp" { -timestamp }
                "depositor" {
                    "depositor_name" { -DEPOSITOR }
                    "email_address" { -email }
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
                                contributors.forEach { contributor ->
                                    "person_name" {
                                        attribute("contributor_role", "author")
                                        attribute("sequence", "first")
                                        contributor.name?.let { "given_name" { -it } }
                                        "surname" { -contributor.surname }
                                        "affiliation" { -contributor.affiliation }
                                        contributor.orcid?.let { orcid ->
                                            "ORCID" {
                                                attribute("authenticated", "false")
                                                -"$ORCID_URL/$orcid"
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
        }.toString(PrintOptions(pretty = true, singleLineTextElements = true, indent = " "))
    }

    companion object {
        const val BS_DOI_ID = "10.6019"
        const val BS_TITLE = "BioStudies Database"
        const val DEPOSITOR = "EMBL-EBI"
        const val ORCID_URL = "https://orcid.org"
        const val XML_NAMESPACE = "http://www.crossref.org/schema/4.4.1"
        const val XML_SCHEMA_VERSION = "4.4.1"
        const val XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance"
        const val XML_SCHEMA_LOCATION_1 = "http://www.crossref.org/schema/4.4.1"
        const val XML_SCHEMA_LOCATION_2 = "http://www.crossref.org/schema/deposit/crossref4.4.1.xsd"
    }
}
