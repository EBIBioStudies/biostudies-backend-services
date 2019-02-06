package ac.uk.ebi.biostd.xml.common

import org.xml.sax.InputSource
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory

fun createXmlDocument(xml: String) =
    DocumentBuilderFactory
        .newInstance()
        .newDocumentBuilder()
        .parse(InputSource(StringReader(xml))).documentElement
