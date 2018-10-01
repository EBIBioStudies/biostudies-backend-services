package ac.uk.ebi.biostd.serialization.json.serialization

import ac.uk.ebi.biostd.serialization.json.common.writeJsonArray
import ac.uk.ebi.biostd.submission.Table
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class TableJsonSerializer : StdSerializer<Table<*>>(Table::class.java) {
    override fun serialize(table: Table<*>, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeJsonArray(table.elements)
    }
}
