package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

typealias Attributes = MutableList<AttributeDetail>

data class Attribute(
    var name: String,
    var value: String,
    var reference: Boolean = false,
    var nameAttrs: Attributes = mutableListOf(),
    var valueAttrs: Attributes = mutableListOf()
) {

    constructor(name: Any, value: Any) : this(name.toString(), value.toString())

    companion object {
        val EMPTY_ATTR: Attribute = Attribute(EMPTY, EMPTY, false, mutableListOf())
    }
}

data class AttributeDetail(val name: String, val value: String)