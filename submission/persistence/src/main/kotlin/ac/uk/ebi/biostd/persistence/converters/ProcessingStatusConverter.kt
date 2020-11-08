package ac.uk.ebi.biostd.persistence.converters

import ebi.ac.uk.model.constants.ProcessingStatus
import javax.persistence.AttributeConverter

internal class ProcessingStatusConverter : AttributeConverter<ProcessingStatus, String> {
    override fun convertToDatabaseColumn(attribute: ProcessingStatus): String = attribute.value

    override fun convertToEntityAttribute(dbData: String): ProcessingStatus =
        ProcessingStatus.valueOf(dbData)
}
