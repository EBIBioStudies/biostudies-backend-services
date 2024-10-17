package ac.uk.ebi.biostd.client.demo

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ebi.ac.uk.dsl.attribute
import ebi.ac.uk.dsl.file
import ebi.ac.uk.dsl.link
import ebi.ac.uk.dsl.section
import ebi.ac.uk.dsl.submission
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.extensions.title
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.nio.file.Files

private val logger = KotlinLogging.logger {}

/**
 * Demonstrate how to submit a simple submission using bio-studies client and kotlin submission dsl. Note that
 *
 * 1. Client Serialization service is used to serialize class representation into json.
 * 2.  Files are uploaded into user file directory first.
 */
fun main(args: Array<String>) =
    runBlocking {
        val client =
            SecurityWebClient
                .create(baseUrl = args[0])
                .getAuthenticatedClient(args[1], args[2])

        val directory = Files.createTempDirectory("submission-files")
        val submissionFile = Files.createTempFile(directory, "sub-file-", ".tmp").toFile()

        client.uploadFiles(listOf(submissionFile))
        val response =
            client.submit(
                submission {
                    title = "my-submission example"
                    attribute("Attribute", "Attr Value 1")
                    attribute("Another-Attribute", "Another-Attribute Value")
                    section("study") {
                        attributes = listOf(Attribute("attribute-1", "attributeValue"))
                        link("AF069309") {
                            attribute("type", "gen")
                        }

                        file(submissionFile.name) {
                            size = 0
                            attribute("Description", "Data File 1")
                        }

                        section("Author") {
                            attribute("Name", "Mr Doctor")
                        }
                    }
                },
            )

        logger.info { "successfully created submission ${response.body.accNo}" }
    }
