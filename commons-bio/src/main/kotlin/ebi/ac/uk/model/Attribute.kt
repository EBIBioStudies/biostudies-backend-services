package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var nameAttrs: MutableList<AttributeDetail> = mutableListOf(),
        var valueAttrs: MutableList<AttributeDetail> = mutableListOf()) {

    constructor(name: Any, value: Any) : this(name.toString(), value.toString())

    companion object {
        val EMPTY_ATTR: Attribute = Attribute(EMPTY, EMPTY, false, mutableListOf())
    }
}

data class AttributeDetail(val name: String, val value: String)