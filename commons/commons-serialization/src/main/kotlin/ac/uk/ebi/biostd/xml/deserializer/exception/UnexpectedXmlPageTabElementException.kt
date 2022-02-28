package ac.uk.ebi.biostd.xml.deserializer.exception

const val UNEXPECTED_XML_ELEMENT_ERROR = "The given page tab element doesn't match with the provided class."

class UnexpectedXmlPageTabElementException : RuntimeException(UNEXPECTED_XML_ELEMENT_ERROR)

class UnexpectedXmlStartElementTypeException(val type: String) :
    RuntimeException("Start element of type \"$type\" is expected.")

class UnexpectedXmlEndElementTypeException(val type: String) :
    RuntimeException("End element of type \"$type\" is expected.")
