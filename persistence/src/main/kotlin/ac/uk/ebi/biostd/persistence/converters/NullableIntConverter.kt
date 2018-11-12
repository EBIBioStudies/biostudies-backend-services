package ac.uk.ebi.biostd.persistence.converters

import javax.persistence.AttributeConverter

internal class NullableIntConverter : AttributeConverter<Int, Int?> {

    override fun convertToDatabaseColumn(attribute: Int?) = attribute

    override fun convertToEntityAttribute(dbData: Int?) = dbData ?: 0
}