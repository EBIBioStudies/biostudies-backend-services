package ebi.ac.uk.model

import ebi.ac.uk.util.collections.indexOf

interface Attributable {
    var attributes: List<Attribute>

    operator fun get(attr: Any): String = find(attr)!!

    fun find(attr: Any): String? = attributes.find { it.name == attr.toString() }?.value

    operator fun set(attr: Any, value: String) {
        when (val attribute = attributes.find { it.name == attr.toString() }) {
            null -> attributes = attributes + Attribute(attr.toString(), value)
            else -> attribute.value = value
        }
    }

    fun addAttribute(attr: Attribute) {
        when (val index = attributes.indexOf { it.name == attr.name }) {
            null -> attributes = attributes + attr
            else -> attributes = attributes.toMutableList().apply { set(index, attr) }
        }
    }
}
