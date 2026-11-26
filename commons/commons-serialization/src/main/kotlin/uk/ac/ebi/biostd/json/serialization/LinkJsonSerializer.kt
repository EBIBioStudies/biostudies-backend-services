package uk.ac.ebi.biostd.json.serialization

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import uk.ac.ebi.biostd.json.common.writeJsonArray
import uk.ac.ebi.biostd.json.common.writeJsonString
import uk.ac.ebi.biostd.json.common.writeObj
import uk.ac.ebi.biostd.model.Link
import uk.ac.ebi.biostd.model.constants.LinkFields

internal class LinkJsonSerializer : StdSerializer<Link>(Link::class.java) {
    override fun isEmpty(
        provider: SerializerProvider,
        value: Link,
    ): Boolean = value.url.isEmpty()

    override fun serialize(
        link: Link,
        gen: JsonGenerator,
        provider: SerializerProvider,
    ) {
        gen.writeObj {
            writeJsonString(LinkFields.URL, link.url)
            writeJsonArray(LinkFields.ATTRIBUTES, link.attributes)
        }
    }
}
