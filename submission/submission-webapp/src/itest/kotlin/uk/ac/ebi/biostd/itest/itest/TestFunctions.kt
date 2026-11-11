package uk.ac.ebi.biostd.itest.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import uk.ac.ebi.biostd.itest.entities.TestUser

fun getWebClient(
    serverPort: Int,
    user: TestUser,
): BioWebClient =
    SecurityWebClient
        .create("http://localhost:$serverPort")
        .getAuthenticatedClient(user.email, user.password)

fun getWebClient(
    serverPort: Int,
    user: TestUser,
    onBehalf: TestUser,
): BioWebClient =
    SecurityWebClient
        .create("http://localhost:$serverPort")
        .getAuthenticatedClient(user.email, user.password, onBehalf.email)
