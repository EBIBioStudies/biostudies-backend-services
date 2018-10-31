package ebi.ac.uk.model

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
