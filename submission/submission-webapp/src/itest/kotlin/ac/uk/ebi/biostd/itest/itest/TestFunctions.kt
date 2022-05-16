package ac.uk.ebi.biostd.itest.itest

import ac.uk.ebi.biostd.client.integration.web.BioWebClient
import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import ac.uk.ebi.biostd.itest.entities.TestUser

fun getWebClient(serverPort: Int, user: TestUser): BioWebClient =
    SecurityWebClient
        .create("http://localhost:$serverPort")
        .getAuthenticatedClient(user.email, user.password)
