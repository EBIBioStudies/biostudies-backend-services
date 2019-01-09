package ac.uk.ebi.biostd.xml.desirializer.common

import ac.uk.ebi.biostd.xml.desirializer.getSubNodes
import ac.uk.ebi.biostd.xml.desirializer.toXmlString
import arrow.core.Either
import ebi.ac.uk.model.Table
import ebi.ac.uk.model.constants.OtherFields
import org.w3c.dom.Node

abstract class BaseXmlDeserializer<T : Any> {

    abstract fun deserialize(node: Node): T

    fun deserializeList(node: Node?): List<T> = node?.getSubNodes()?.mapTo(mutableListOf(), this::deserialize).orEmpty()

    fun deserializeList(nodes: List<Node>): List<T> = nodes.mapTo(mutableListOf(), this::deserialize)

    fun <B : Table<T>> deserializeTableList(node: Node?, leftTag: String, tableBuilder: (List<T>) -> B) =
        node?.getSubNodes().orEmpty().mapTo(mutableListOf()) { deserializeTable(it, leftTag, tableBuilder) }

    private fun <B : Table<T>> deserializeTable(node: Node, leftTag: String, tableBuilder: (List<T>) -> B): Either<T, B> {
        return when (node.nodeName) {
            leftTag -> Either.left(deserialize(node))
            OtherFields.TABLE.value -> Either.right(tableBuilder(deserializeList(node)))
            else -> error({
                "expecting node type '${node.nodeName}' to be of type '$leftTag' or " +
                    "'${OtherFields.TABLE.value}' found ${node.toXmlString()}) "
            })
        }
    }
}
