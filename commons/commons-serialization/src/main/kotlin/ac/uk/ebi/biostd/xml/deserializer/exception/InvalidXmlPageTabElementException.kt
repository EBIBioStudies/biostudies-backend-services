package ac.uk.ebi.biostd.xml.deserializer.exception

const val INVALID_XML_ELEMENT_ERROR = "The given element is not valid page tab"

class InvalidXmlPageTabElementException : RuntimeException(INVALID_XML_ELEMENT_ERROR)
