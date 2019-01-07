package ac.uk.ebi.biostd.json.serialization

import ac.uk.ebi.biostd.json.common.writeJsonArray
import ac.uk.ebi.biostd.json.common.writeJsonString
import ac.uk.ebi.biostd.json.common.writeObj
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import ebi.ac.uk.model.Link
import ebi.ac.uk.model.constants.LinkFields

class LinkJsonSerializer : StdSerializer<Link>(Link::class.java) {

    override fun isEmpty(provider: SerializerProvider, value: Link): Boolean = value.url.isEmpty()

    override fun serialize(link: Link, gen: JsonGenerator, provider: SerializerProvider) {

        gen.writeObj {
            writeJsonString(LinkFields.URL, link.url)
            writeJsonArray(LinkFields.ATTRIBUTES, link.attributes)
        }
    }
}
