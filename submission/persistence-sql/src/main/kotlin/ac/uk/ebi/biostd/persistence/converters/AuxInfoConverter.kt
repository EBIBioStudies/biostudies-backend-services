package ac.uk.ebi.biostd.persistence.converters

import ac.uk.ebi.biostd.persistence.model.AuxInfo
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.Marshaller
import java.io.StringReader
import java.io.StringWriter
import javax.persistence.AttributeConverter

internal class AuxInfoConverter : AttributeConverter<AuxInfo, String> {
    private val jaxbContext: JAXBContext = JAXBContext.newInstance(AuxInfo::class.java)

    override fun convertToDatabaseColumn(auxInfo: AuxInfo): String {
        val sw = StringWriter()
        createMarshaller().marshal(auxInfo, sw)
        return sw.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): AuxInfo {
        return if (dbData == null)
            AuxInfo()
        else
            jaxbContext.createUnmarshaller().unmarshal(StringReader(dbData)) as AuxInfo
    }

    private fun createMarshaller(): Marshaller {
        val marshaller = jaxbContext.createMarshaller()
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, java.lang.Boolean.TRUE)
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, java.lang.Boolean.TRUE)
        return marshaller
    }
}
