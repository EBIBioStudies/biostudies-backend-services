package ac.uk.ebi.biostd.serialization.xml.serializer

import ac.uk.ebi.biostd.submission.Section
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class SectionSerializer : StdSerializer<Section>(Section::class.java) {

    override fun serialize(value: Section?, gen: JsonGenerator?, provider: SerializerProvider?) {
        gen?.writeString("jola")
    }
}
