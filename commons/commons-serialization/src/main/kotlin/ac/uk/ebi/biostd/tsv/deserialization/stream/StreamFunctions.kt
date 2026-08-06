package ac.uk.ebi.biostd.tsv.deserialization.stream

import ac.uk.ebi.biostd.tsv.TAB
import ac.uk.ebi.biostd.validation.InvalidElementException
import ac.uk.ebi.biostd.validation.REQUIRED_ATTR_NAME
import ebi.ac.uk.io.ext.asFlow
import ebi.ac.uk.model.Attribute
import ebi.ac.uk.util.collections.destructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.InputStream

internal suspend fun <T> BufferedWriter.writeElements(
    elements: Flow<T>,
    tableName: String,
    identifier: (T) -> String,
    attributes: (T) -> List<Attribute>,
) {
    elements.collectIndexed { index, element ->
        if (index == 0) writeHeaders(tableName, attributes(element))
        writeValues(identifier(element), attributes(element))
    }
}

private suspend fun BufferedWriter.writeHeaders(
    tableName: String,
    attributes: List<Attribute>,
) = withContext(Dispatchers.IO) {
    write(tableName)
    if (attributes.isNotEmpty()) write(TAB + attributes.joinToString(TAB.toString()) { it.name })
    newLine()
}

private suspend fun BufferedWriter.writeValues(
    value: String,
    attributes: List<Attribute>,
) = withContext(Dispatchers.IO) {
    write(value)
    if (attributes.isNotEmpty()) write(TAB + attributes.joinToString(TAB.toString()) { it.value.orEmpty() })
    newLine()
}

internal fun <T> InputStream.readElements(
    expectedTable: String,
    invalidTableMessage: String,
    rowMapper: (Int, String, List<String>) -> T,
): Flow<T> {
    val reader = bufferedReader()
    val (table, headers) = reader.readLine().split(TAB).destructure()
    require(table == expectedTable) { throw InvalidElementException(invalidTableMessage) }
    require(headers.none { it.isBlank() }) { throw InvalidElementException(REQUIRED_ATTR_NAME) }

    return reader
        .asFlow()
        .filter { it.isNotBlank() }
        .withIndex()
        .map { (index, row) -> rowMapper(index + 1, row, headers) }
}

internal fun buildAttributes(
    fields: List<String>,
    headers: List<String>,
): List<Attribute> = fields.take(headers.size).mapIndexed { index, value -> Attribute(headers[index], value) }
