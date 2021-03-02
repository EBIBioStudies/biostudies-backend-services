package uk.ac.ebi.biostd.client.cli

import ac.uk.ebi.biostd.client.integration.web.SecurityWebClient
import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

internal fun getClient(host: String, user: String, password: String, onBehalf: String?) =
    if (onBehalf.isNullOrBlank()) SecurityWebClient.create(host).getAuthenticatedClient(user, password)
    else SecurityWebClient.create(host).getAuthenticatedClient(user, password, onBehalf)

internal fun formatErrorMessage(message: String): String {
    val xmlOutput = StreamResult(StringWriter())
    val xmlInput = StreamSource(StringReader(message))
    val transformer =
        TransformerFactory.newInstance()
            .apply { setAttribute("indent-number", 2) }
            .newTransformer()

    transformer.setOutputProperty(OutputKeys.INDENT, "yes")
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
    transformer.transform(xmlInput, xmlOutput)

    return xmlOutput.writer.toString()
}
