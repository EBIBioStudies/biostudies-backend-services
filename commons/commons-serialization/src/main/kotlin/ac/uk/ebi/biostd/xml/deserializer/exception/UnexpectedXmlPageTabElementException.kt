package ac.uk.ebi.biostd.xml.deserializer.exception

const val UNEXPECTED_XML_ELEMENT_ERROR = "The given pate tab element doesn't match with the provided class"

class UnexpectedXmlPageTabElementException : RuntimeException(UNEXPECTED_XML_ELEMENT_ERROR)
