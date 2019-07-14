package ebi.ac.uk.model.builders

import ebi.ac.uk.model.Attribute

class AttributeBuilder {

    var name: String? = null
    var value: String? = null

    fun build(): Attribute = Attribute(
        requireNotNull(name) { "attribute name need to be defined" },
        requireNotNull(value) { "attribute value need to be defined" }
    )
}
