package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.serialization.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlField
import ac.uk.ebi.biostd.serialization.xml.extensions.writeXmlObj
import ac.uk.ebi.biostd.submission.FileFields
import ac.uk.ebi.biostd.submission.FilesTable
import ac.uk.ebi.biostd.submission.LinkFields
import ac.uk.ebi.biostd.submission.LinksTable
import ac.uk.ebi.biostd.submission.OtherFields
import ac.uk.ebi.biostd.submission.SectionFields
import ac.uk.ebi.biostd.submission.SectionsTable
import ac.uk.ebi.biostd.submission.Table
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator

class TableSerializer : XmlStdSerializer<Table<*>>(Table::class.java) {

    override fun serializeXml(value: Table<*>, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(OtherFields.TABLE, value) {
                value.elements.forEach {
                    when (value) {
                        is LinksTable -> writeXmlField(LinkFields.LINK, it)
                        is SectionsTable -> writeXmlField(SectionFields.SECTION, it)
                        is FilesTable -> writeXmlField(FileFields.FILE, it)
                    }
                }
            }
        }
    }
}
