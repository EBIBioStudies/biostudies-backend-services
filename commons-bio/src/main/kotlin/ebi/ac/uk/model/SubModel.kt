package ebi.ac.uk.model

import arrow.core.Either

data class File(var name: String) : Attributable() {

    constructor(url: String, attributes: List<Attribute>) : this(url) {
        this.attributes = attributes.toMutableList()
    }
}

data class Link(var url: String) : Attributable() {

    constructor(url: String, attributes: List<Attribute>) : this(url) {
        this.attributes = attributes.toMutableList()
    }
}

data class AttributeDetail(val name: String, val value: String)

class Submission : Attributable() {
    var accessTags: MutableList<String> = mutableListOf()
    var rootSection: Section = Section()
}

data class User(var email: String) {

    var secretKey: String = ""
    val id: Long = 0
}

open class Attributable() {

    private val attributesMap = mutableMapOf<String, Attribute>()

    open var attributes: MutableList<Attribute>
        get() = this.attributesMap.values.toMutableList()
        set(value) {
            attributesMap.clear()
            value.forEach { attributesMap[it.name] = it }
        }

    constructor(attributes: List<Attribute>) : this() {
        attributes.forEach { this.attributesMap[it.name] = it }
    }

    operator fun <T> get(attr: Any) = attributesMap[attr.toString()]!!.value as T

    fun <T> find(attr: Any) = attributesMap[attr.toString()]?.value as T?

    operator fun set(attr: Any, value: Any) {
        attributesMap[attr.toString()] = Attribute(attr.toString(), value.toString())
    }

}


data class Section(
        var sections: MutableList<Either<Section, SectionsTable>> = mutableListOf(),
        val files: MutableList<Either<File, FilesTable>> = mutableListOf(),
        val links: MutableList<Either<Link, LinksTable>> = mutableListOf()) : Attributable()

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
