package ebi.ac.uk.model

import ebi.ac.uk.model.extensions.Section
import java.util.Objects.equals
import java.util.Objects.hash

class Submission(
        var rootSection: Section = Section(),
        var accessTags: MutableList<String> = mutableListOf(),
        attributes: List<Attribute> = emptyList()) : Attributable(attributes)

class File(var name: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?): Boolean {
        other as? File ?: return false
        if (this === other) return true

        return equals(this.name, other.name).and(
                equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode(): Int {
        return hash(this.name, this.attributesMap)
    }
}

class Link(var url: String, attributes: List<Attribute> = emptyList()) : Attributable(attributes) {

    override fun equals(other: Any?): Boolean {
        other as? Link ?: return false
        if (this === other) return true

        return equals(this.url, other.url).and(
                equals(this.attributesMap, other.attributesMap))
    }

    override fun hashCode(): Int {
        return hash(this.url, this.attributesMap)
    }
}


data class AttributeDetail(val name: String, val value: String)
class User(var email: String, var secretKey: String = "", val id: Long = 0)

open class Attributable() {

    protected val attributesMap = mutableMapOf<String, Attribute>()

    open val attributes: List<Attribute>
        get() = this.attributesMap.values.toMutableList()

    constructor(attributes: List<Attribute>) : this() {
        attributes.forEach { this.attributesMap[it.name] = it }
    }

    operator fun <T> get(attr: Any) = attributesMap[attr.toString()]?.value as T

    fun <T> find(attr: Any) = attributesMap[attr.toString()]?.value as T?

    operator fun set(attr: Any, value: Any) = attributesMap.set(attr.toString(), Attribute(attr.toString(), value.toString()))

    fun addAttribute(attr: Attribute) = attributesMap.set(attr.name, attr)

}

data class Attribute(
        var name: String,
        var value: String,
        var reference: Boolean = false,
        var nameAttrs: MutableList<AttributeDetail> = mutableListOf(),
        var valueAttrs: MutableList<AttributeDetail> = mutableListOf()) {

    constructor(name: Any, value: Any) : this(name.toString(), value.toString())

    companion object {
        val EMPTY_ATTR: Attribute = Attribute("", "", false, mutableListOf())
    }
}
