package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.FileFields
import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlCollection
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class FileSerializer : XmlStdSerializer<File>(File::class.java) {

    override fun serializeXml(value: File, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(FileFields.FILE, value) {
                writeXmlField(FileFields.NAME, value.name)
                writeXmlField(FileFields.TYPE, value.type)
                writeXmlField(FileFields.SIZE, value.size)
                writeXmlCollection(FileFields.ATTRIBUTES, value.attributes)
            }
        }
    }
}
