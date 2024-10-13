package ac.uk.ebi.biostd.client.common

import org.springframework.util.LinkedMultiValueMap

class MultipartBuilder {
    private val map: MutableMap<String, MutableList<Any>> = mutableMapOf()

    fun add(
        key: String,
        value: Any?,
    ): MultipartBuilder {
        if (value != null) map.computeIfAbsent(key) { mutableListOf() }.add(value)
        return this
    }

    fun addAll(
        key: String,
        values: List<Any>,
    ): MultipartBuilder {
        map.computeIfAbsent(key) { mutableListOf() }.addAll(values)
        return this
    }

    fun build(): LinkedMultiValueMap<String, Any> = LinkedMultiValueMap(map.toMap())
}
