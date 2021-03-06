package ac.uk.ebi.biostd.xml.serializer

import ac.uk.ebi.biostd.xml.common.XmlStdSerializer
import ac.uk.ebi.biostd.xml.common.writeXmlField
import ac.uk.ebi.biostd.xml.common.writeXmlObj
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator
import ebi.ac.uk.model.FilesTable
import ebi.ac.uk.model.LinksTable
import ebi.ac.uk.model.SectionsTable
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.FileFields
import ebi.ac.uk.model.constants.LinkFields
import ebi.ac.uk.model.constants.OtherFields
import ebi.ac.uk.model.constants.SectionFields

class TableSerializer : XmlStdSerializer<Table<*>>(Table::class.java) {

    override fun serializeXml(value: Table<*>, gen: ToXmlGenerator, provider: SerializerProvider) {
        with(gen) {
            writeXmlObj(OtherFields.TABLE) {
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
