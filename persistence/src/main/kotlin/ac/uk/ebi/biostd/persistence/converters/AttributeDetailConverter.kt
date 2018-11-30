package ac.uk.ebi.biostd.persistence.converters

import ac.uk.ebi.biostd.persistence.model.AttributeDetail
import ebi.ac.uk.util.collections.second
import javax.persistence.AttributeConverter

const val ATTR_SEP = ";"
const val ATTR_REL = "="

class AttributeDetailConverter : AttributeConverter<MutableList<AttributeDetail>, String> {
    override fun convertToDatabaseColumn(attribute: MutableList<AttributeDetail>) =
        attribute.fold("") { converted, current ->
            converted + current.name + ATTR_REL + current.value + ATTR_SEP }.removeSuffix(ATTR_SEP)

    override fun convertToEntityAttribute(dbData: String?): MutableList<AttributeDetail> =
        dbData.orEmpty()
            .split(ATTR_SEP)
            .dropWhile { it.isEmpty() }
            .map { it.split(ATTR_REL) }
            .mapTo(mutableListOf()) { AttributeDetail(it.first(), it.second()) }
}
