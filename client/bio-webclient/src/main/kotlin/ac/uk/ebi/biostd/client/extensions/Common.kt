package ac.uk.ebi.biostd.client.extensions

import org.springframework.http.HttpHeaders
import org.springframework.util.LinkedMultiValueMap

fun <K, V> LinkedMultiValueMap(vararg pairs: Pair<K, V>): LinkedMultiValueMap<K, V> {
    val map = pairs.groupBy({ it.first }, { it.second })
    return LinkedMultiValueMap(map)
}

fun HttpHeaders(vararg pairs: Pair<String, Any>): HttpHeaders {
    val values = pairs.map { it.first to it.second.toString() }.toTypedArray()
    return HttpHeaders(LinkedMultiValueMap(*values))
}
