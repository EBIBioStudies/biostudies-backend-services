package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlAttr
import ac.uk.ebi.biostd.xml.common.writeXmlCollection
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.File
import ebi.ac.uk.model.constants.FileFields

class FileSerializer : XmlStdSerializer<File>(File::class.java) {

    override fun serializeXml(value: File, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(FileFields.FILE) {
                writeXmlAttr(FileFields.SIZE, value.size)
                writeXmlField(FileFields.PATH, value.path)
                writeXmlField(FileFields.TYPE, value.type)
                writeXmlCollection(FileFields.ATTRIBUTES, value.attributes)
            }
        }
    }
}
