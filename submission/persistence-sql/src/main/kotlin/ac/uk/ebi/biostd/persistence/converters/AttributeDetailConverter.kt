package ac.uk.ebi.biostd.persistence.converters

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.util.collections.second
import javax.persistence.AttributeConverter

const val ATTR_SEP = ";"
const val ATTR_REL = "="

internal class AttributeDetailConverter : AttributeConverter<MutableList<AttributeDetail>, String> {
    override fun convertToDatabaseColumn(attributes: MutableList<AttributeDetail>) =
        attributes.joinToString(separator = ATTR_SEP) { "${it.name}=${it.value}" }

    override fun convertToEntityAttribute(dbData: String?): MutableList<AttributeDetail> =
        dbData.orEmpty()
            .split(ATTR_SEP)
            .dropWhile { it.isEmpty() }
            .map { it.split(ATTR_REL) }
            .filterNot { it.second().isBlank() }
            .mapTo(mutableListOf()) { AttributeDetail(it.first(), it.second()) }
}
