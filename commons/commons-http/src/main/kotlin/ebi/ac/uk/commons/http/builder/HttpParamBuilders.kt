package ebi.ac.uk.commons.http.builder

import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap

fun <K, V> linkedMultiValueMapOf(pairs: List<Pair<K, V>>): LinkedMultiValueMap<K, V> {
    val map = pairs.groupBy({ it.first }, { it.second })
    return LinkedMultiValueMap(map)
}

@Suppress("SpreadOperator")
fun <K, V> linkedMultiValueMapOf(vararg pairs: Pair<K, V>): LinkedMultiValueMap<K, V> {
    val map = pairs.groupBy({ it.first }, { it.second })
    return LinkedMultiValueMap(map)
}

@Suppress("SpreadOperator")
fun httpHeadersOf(vararg pairs: Pair<String, Any>): HttpHeaders {
    val values = pairs.map { it.first to it.second.toString() }.toTypedArray()
    return HttpHeaders(linkedMultiValueMapOf(*values))
}
