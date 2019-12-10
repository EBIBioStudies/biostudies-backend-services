package ebi.ac.uk.model

import ebi.ac.uk.base.EMPTY
import java.util.Objects

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

    override fun equals(other: Any?) = when {
        other !is Attribute -> false
        this === other -> true
        else -> name.equals(other.name, ignoreCase = true)
            .and(value.equals(other.value, ignoreCase = true))
            .and(Objects.equals(reference, other.reference))
            .and(Objects.equals(nameAttrs, other.nameAttrs))
            .and(Objects.equals(valueAttrs, other.valueAttrs))
    }

    override fun hashCode() = Objects.hash(name, value, reference, nameAttrs, valueAttrs)
}

data class AttributeDetail(val name: String, val value: String) {
    override fun equals(other: Any?) = when {
        other !is AttributeDetail -> false
        this === other -> true
        else -> name.equals(other.name, ignoreCase = true).and(value.equals(other.value, ignoreCase = true))
    }

    override fun hashCode() = Objects.hash(name.toLowerCase(), value.toLowerCase())
}

fun attributeDetails(name: String, value: String) = mutableListOf(AttributeDetail(name, value))
