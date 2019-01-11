package ac.uk.ebi.biostd.xml.deserializer

class TestDeserializerFactory {
    companion object {
        fun attributeXmlDeserializer() = AttributeXmlDeserializer(DetailsXmlDeserializer())

        fun fileXmlDeserializer() = FileXmlDeserializer(attributeXmlDeserializer())

        fun linkXmlDeserializer() = LinkXmlDeserializer(attributeXmlDeserializer())

        fun sectionXmlDeserializer() =
            SectionXmlDeserializer(attributeXmlDeserializer(), linkXmlDeserializer(), fileXmlDeserializer())

        fun submissionXmlDeserializer() = SubmissionXmlDeserializer(attributeXmlDeserializer(), sectionXmlDeserializer())
    }
}
