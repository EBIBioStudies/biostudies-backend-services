package uk.ac.ebi.biostd.client.cli

import java.io.StringReader
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

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