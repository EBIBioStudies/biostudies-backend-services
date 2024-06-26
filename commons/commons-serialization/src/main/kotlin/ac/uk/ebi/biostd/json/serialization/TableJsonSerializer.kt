package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Table

internal class TableJsonSerializer : StdSerializer<Table<*>>(Table::class.java) {
    override fun serialize(
        table: Table<*>,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeJsonArray(table.elements)
    }
}
