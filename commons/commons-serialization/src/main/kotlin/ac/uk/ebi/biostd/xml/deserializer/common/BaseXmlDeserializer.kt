package ac.uk.ebi.biostd.xml.deserializer.common

import ac.uk.ebi.biostd.xml.deserializer.getSubNodes
import ac.uk.ebi.biostd.xml.deserializer.toXmlString
import arrow.core.Either
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.OtherFields.TABLE
import org.w3c.dom.Node

abstract class BaseXmlDeserializer<T : Any> {
    abstract fun deserialize(node: Node): T

    fun deserializeList(node: Node?): List<T> = node?.getSubNodes()?.mapTo(mutableListOf(), ::deserialize).orEmpty()

    fun deserializeList(nodes: List<Node>): List<T> = nodes.mapTo(mutableListOf(), ::deserialize)

    fun <B : Table<T>> deserializeTableList(node: Node?, leftTag: String, tableBuilder: (List<T>) -> B) =
        node?.getSubNodes().orEmpty().mapTo(mutableListOf()) { deserializeTable(it, leftTag, tableBuilder) }

    protected fun <B : Table<T>> deserializeTable(
        node: Node,
        leftTag: String,
        tableBuilder: (List<T>) -> B
    ): Either<T, B> = when (node.nodeName) {
        leftTag -> Either.left(deserialize(node))
        TABLE.value -> Either.right(tableBuilder(deserializeList(node)))
        else -> error({ errorMessage(node, leftTag) })
    }

    private fun errorMessage(node: Node, leftTag: String) =
        "expecting node type '${node.nodeName}' to be of type '$leftTag' or '$TABLE' found ${node.toXmlString()})"
}
