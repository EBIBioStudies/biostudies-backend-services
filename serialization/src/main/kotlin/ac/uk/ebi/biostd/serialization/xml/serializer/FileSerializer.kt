package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlCollection
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlField
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlObj
import ac.uk.ebi.biostd.submission.File
import ac.uk.ebi.biostd.submission.FileFields
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class FileSerializer : XmlStdSerializer<File>(File::class.java) {

    override fun serializeXml(value: File, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(FileFields.FILE, value) {
                writeXmlField(FileFields.NAME, value.name)
                writeXmlField(FileFields.SIZE, value.size)
                writeXmlField(FileFields.TYPE, value.type)
                writeXmlCollection(FileFields.ATTRIBUTES, value.attributes)
            }
        }
    }
}
