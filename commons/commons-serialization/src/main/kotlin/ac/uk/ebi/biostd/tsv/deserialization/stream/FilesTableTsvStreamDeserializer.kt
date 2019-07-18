package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.deserialization.model.TsvChunkLine
import ac.uk.ebi.biostd.validation.InvalidElementException
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.model.File

internal class FilesTableTsvStreamDeserializer(pageTabHeader: String) {
    var header = TsvChunkLine(0, pageTabHeader)

    fun deserializeRow(row: String, index: Int = 0): File {
        val fields = TsvChunkLine(index, row)
        return File(fields.name, attributes = buildAttributes(fields))
    }

    private fun buildAttributes(fields: TsvChunkLine): List<Attribute> {
        val attributeNames = header.rawValues
        val attributeValues = fields.rawValues

        if (attributeValues.size != attributeNames.size) {
            throw InvalidElementException(
                "Error at row # ${fields.index}. The row must have the same attributes as the header")
        }

        return attributeNames.mapIndexed { index, name -> Attribute(name, attributeValues[index]) }
    }
}
